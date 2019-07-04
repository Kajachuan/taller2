from datetime import datetime
from taller2.app.app import db
from taller2.app.models.message import Message
from taller2.app.models.forbidden_words import ForbiddenWords

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
        assert self.message.type == 'text'

    def test_type_message(self):
        snippet_message = Message(timestamp=datetime.now(), sender='User', message='some code', creation_date=datetime.now(),type='snippet')
        assert snippet_message.type == 'snippet'
        assert snippet_message.type

    def test_mention(self):
        mention_message = Message(timestamp = datetime.now(), sender = 'UserName', message = 'hola @tuvieja', creation_date = datetime.now())
        assert mention_message.has_mention() == True
        assert mention_message.get_mentioned_and_command()[0] == 'tuvieja'

    def test_ban_forbidden_word(self):
        ForbiddenWords.add_word('ptm')
        new_message = Message(timestamp = datetime.now(), sender = 'UserName', message = 'hola ptm hola', creation_date = datetime.now())
        new_message.save()
        assert new_message.message == 'hola *** hola'

    def test_ban_forbidden_word_case_insensitive(self):
        ForbiddenWords.add_word('LRPM')
        new_message = Message(timestamp = datetime.now(), sender = 'UserName', message = 'hola LRPM hola PTM', creation_date = datetime.now())
        new_message.save()
        assert new_message.message == 'hola **** hola ***'
