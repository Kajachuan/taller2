from ..app import db

class Message(db.Document):
    timestamp = db.DateField(required = True)
    sender = db.StringField(required = True)
    message = db.StringField(required = True)
    meta = {'strict': False}
