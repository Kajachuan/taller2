from ..app import db
from ..exceptions.channel_error import InvalidAmountOfMembers
from .message import Message
from datetime import datetime

class DirectChannel(db.Document):
    members = db.ListField(db.ReferenceField('User'), required = True)
    messages = db.ListField(db.ReferenceField('Message'))
    channel_name = db.StringField()

    def clean(self):
        if len(self.members) != 2:
            raise InvalidAmountOfMembers
        self.channel_name = self.members[0].username

    def get_messages(self, since = 1, to = 1):
        start = len(self.messages) - to
        end = len(self.messages) - since + 1
        start = 0 if start < 0 else start
        return self.messages[start: end]

    @classmethod
    def add_message(cls, organization_name, sender, receiver, message):
        from .organization import Organization
        organization = Organization.objects.get(organization_name = organization_name)
        for orga_dchannel in organization.direct_channels:
            if orga_dchannel.channel_name == sender or orga_dchannel.channel_name == receiver:
                dchannel = orga_dchannel
        message = Message(timestamp = datetime.today(), sender = sender, message = message, creation_date = datetime.now())
        message.save()
        dchannel.update(push__messages = message)
