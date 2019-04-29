from os import environ
from cryptography.fernet import Fernet
from ..app import db
from ..exceptions.register_error import BlankUsernameError, InvalidEmailError, DuplicateUsernameError

class User(db.Document):
    username = db.StringField(required=True, unique=True, min_length=1)
    email = db.EmailField(required=True)
    crypted_password = db.StringField(required=True)
    first_name = db.StringField(required=False)
    last_name = db.StringField(required=False)
    meta = {'strict': False}

    def clean(self):
        if self.username == "":
            raise BlankUsernameError()
        if User.objects(username=self.username).count():
            raise DuplicateUsernameError()
        try:
            db.EmailField.validate(db.EmailField(), self.email)
        except:
            raise InvalidEmailError()

    @classmethod
    def authenticate(cls, username, password):
        try:
            user = cls.objects.get(username=username)
        except:
            return None

        if not user.has_password(password):
            return None
        return user

    def has_password(self, password):
        cipher_suite = Fernet(environ['CRYPT_KEY'].encode())
        return password == cipher_suite.decrypt(self.crypted_password.encode()).decode()
