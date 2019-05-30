from datetime import datetime
from taller2.app.app import db
from taller2.app.models.message import Message

class TestMessage(object):
    def setup_class(cls):
        cls.message = Message(timestamp = datetime.now(), sender = 'UserName', message = 'hola', creation_date = datetime.now())
        cls.message.save()

    def test_info(self):
        assert self.message.message == 'hola'
        assert self.message.sender == 'UserName'
        assert self.message.timestamp.day == datetime.today().day
        assert self.message.timestamp.month == datetime.today().month
        assert self.message.timestamp.year == datetime.today().year
