from ..app import db
from ..exceptions.channel_error import InvalidAmountOfMembers

class DirectChannel(db.Document):
    members = db.ListField(db.ReferenceField('User'), required = True)
    messages = db.ListField(db.ReferenceField('Message'))
    channel_name = db.StringField()

    def clean(self):
        if len(self.members) != 2:
            raise InvalidAmountOfMembers
        self.channel_name = self.members[0].username
