from os import environ
from cryptography.fernet import Fernet

try:
    from ..app import db
except:
    from app import db

class User(db.Document):
    id = db.SequenceField()
    username = db.StringField(required=True, unique=True, min_length=1)
    email = db.EmailField(required=True)
    crypted_password = db.StringField(required=True)
    first_name = db.StringField(required = False)
    last_name = db.StringField(required = False)
    #profile_picture = db.ImageField(required = False)
    meta = {'strict': False}

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
        try:
            cipher_suite = Fernet(environ['CRYPT_KEY'].encode())
            crypted_password = cipher_suite.encrypt(password.encode())
        except KeyError:
            crypted_password = password
        return self.crypted_password == crypted_password
