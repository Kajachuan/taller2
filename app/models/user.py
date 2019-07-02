from base64 import b64encode
from os import environ, path
from cryptography.fernet import Fernet
from ..app import db
from ..exceptions.register_error import BlankUsernameError, InvalidEmailError, DuplicateUsernameError

class User(db.Document):
    username = db.StringField(required=True, unique=True, min_length=1)
    email = db.EmailField(required=True)
    crypted_password = db.StringField(required=False)
    first_name = db.StringField(required=False)
    last_name = db.StringField(required=False)
    encoded_image = db.StringField(required=False,
                                   default=b64encode(open(path
                                                     .abspath(path
                                                     .join(path
                                                     .dirname(__file__),
                                                     '../static/img/default_image.png')),
                                                     'rb')
                                                     .read())
                                                     .decode())
    invitations = db.DictField()
    organizations = db.ListField(db.StringField())
    recovery_code = db.StringField(required=False)
    creation_date = db.DateTimeField(required=True)
    ban_date = db.DateTimeField(required=False)
    ban_reason = db.StringField(required=False)
    facebook_id = db.StringField(required=False)
    firebase_token = db.StringField(required=False)
    sent_messages = db.IntField(default=0)
    location = db.DictField(required=False, default={'lat':0, 'lon':0})
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

    @classmethod
    def invite(cls,username, token, organization):
        user = cls.objects.get(username = username)
        user.update(**{'set__invitations__' + token: organization})

    @classmethod
    def add_to_organization(cls, username, organization_name):
        user = cls.objects.get(username = username)
        user.update(push__organizations = organization_name)

    @classmethod
    def set_firebase_token(cls, username, token):
        user = cls.objects.get(username = username)
        user.update(firebase_token = token)

    def has_password(self, password):
        cipher_suite = Fernet(environ['CRYPT_KEY'].encode())
        return password == cipher_suite.decrypt(self.crypted_password.encode()).decode()
