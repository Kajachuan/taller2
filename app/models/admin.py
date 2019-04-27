from os import environ
from cryptography.fernet import Fernet
from ..app import db

class Admin(db.Document):
    name = db.StringField(required=True, unique=True)
    crypted_password = db.StringField(required=True)
    meta = {'strict': False}

    @classmethod
    def authenticate(cls, name, password):
        try:
            admin = cls.objects.get(name=name)
        except:
            return None

        if not admin.has_password(password):
            return None
        return admin

    def has_password(self, password):
        cipher_suite = Fernet(environ['CRYPT_KEY'].encode())
        return password == cipher_suite.decrypt(self.crypted_password.encode()).decode()
