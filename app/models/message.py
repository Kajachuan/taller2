from ..app import db
from .forbidden_words import ForbiddenWords
import re

MENTION_SYMBOL = '@'

class Message(db.Document):
    timestamp = db.DateTimeField(required = True)
    sender = db.StringField(required = True)
    message = db.StringField(required = True)
    creation_date = db.DateTimeField(required=True)
    type = db.StringField(required = True, default = 'text')
    meta = {'strict': False}

    def clean(self):
        if self.type != 'text':
            return
        for forbidden_word in ForbiddenWords().get_words():
            if forbidden_word.lower() in self.message.lower():
                fw = re.compile(re.escape(forbidden_word), re.IGNORECASE)
                self.message = fw.sub('*' * len(forbidden_word), self.message)

    def replace_organization_forbidden_words(self, forbidden_words):
        if self.type != 'text':
            return
        for forbidden_word in forbidden_words:
            if forbidden_word.lower() in self.message.lower():
                fw = re.compile(re.escape(forbidden_word), re.IGNORECASE)
                self.message = fw.sub('*' * len(forbidden_word), self.message)

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
