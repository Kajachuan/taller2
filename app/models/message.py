from ..app import db

MENTION_SYMBOL = '@'

class Message(db.Document):
    timestamp = db.DateTimeField(required = True)
    sender = db.StringField(required = True)
    message = db.StringField(required = True)
    creation_date = db.DateTimeField(required=True)
    type = db.StringField(required = True, default = 'message')
    meta = {'strict': False}

    def has_mention(self):
        return True if MENTION_SYMBOL in self.message else False

    def get_mentioned(self):
        parsed = self.message.split(' ')
        for word in parsed:
            if MENTION_SYMBOL in word:
                return word[1:]
