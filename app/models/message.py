from ..app import db

class Message(db.Document):
    timestamp = db.DateTimeField(required = True)
    sender = db.StringField(required = True)
    message = db.StringField(required = True)
    creation_date = db.DateTimeField(required=True)
    type = db.StringField(required = True, default = 'message')
    meta = {'strict': False}
