from http import HTTPStatus
from taller2.app.app import app

client = app.test_client()

class TestUsersController(object):
    def test_new_user(self):
        response = client.post('/register',
                               data='{"username": "MiNombre", "email": "user@test.com",\
                                     "password": "mipass", "password_confirmation": "mipass"}')
        assert response.status_code == HTTPStatus.CREATED
