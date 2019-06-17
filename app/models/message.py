from ..app import db

class Message(db.Document):
    timestamp = db.StringField(required = True)
    sender = db.StringField(required = True)
    message = db.StringField(required = True)
    creation_date = db.DateTimeField(required=True)
    meta = {'strict': False}
