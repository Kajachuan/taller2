import pytest
from ....app.models.user import User

class TestUser(object):
    def test_user_name(self):
        user = User("Gino")
        assert user.name == "Gino"
