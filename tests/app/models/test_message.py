from taller2.app.app import db
from taller2.app.models.message import Message
import datetime as dt

class TestMessage(object):
    def setup_class(cls):
        cls.message = Message(timestamp = dt.datetime.today(), sender = 'UserName', message = 'hola')
        cls.message.save()

    def test_info(self):
        assert self.message.message == 'hola'
        assert self.message.sender == 'UserName'
        assert self.message.timestamp.day == dt.datetime.today().day
        assert self.message.timestamp.month == dt.datetime.today().month
        assert self.message.timestamp.year == dt.datetime.today().year
