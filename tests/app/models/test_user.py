from taller2.app.app import db
from taller2.app.models.user import User

class TestUser(object):
    def test_new_user(self):
        user = User(username='MiNombre', email='user@test.com', crypted_password='mipass')
        assert user.username == 'MiNombre'
        assert user.email == 'user@test.com'
        assert user.crypted_password == 'mipass'
