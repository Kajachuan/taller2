from ..app import db

class User(db.Document):
    id = db.SequenceField()
    username = db.StringField(required=True, unique=True, min_length=1)
    email = db.EmailField(required=True)
    crypted_password = db.StringField(required=True)
