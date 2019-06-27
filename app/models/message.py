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

    def get_mentioned_and_command(self):
        parsed = self.message.split(' ')
        mentioned = ''
        command = ''
        for index in range(len(parsed)):
            if MENTION_SYMBOL in parsed[index]:
                mentioned = parsed[index][1:]
                if index != len(parsed) - 1:
                    command = ' '.join(parsed[index + 1:])
                return (mentioned, command)
