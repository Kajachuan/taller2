from http import HTTPStatus
from taller2.app.app import app

client = app.test_client()

client.post('/register', data='{"username": "orgacreator", "email": "user@test.com",\
                                "password": "mipass", "password_confirmation": "mipass"}')
client.post('/login', data='{"username": "orgacreator", "password": "mipass"}')

class TestOrganizationsController(object):
    def test_correct_creation_of_organization(self):
        response = client.post('/organization', data = '{"name" : "Taller2"}')
        assert response.status_code == HTTPStatus.CREATED

    def test_missing_name(self):
        response = client.post('/organization')
        assert response.status_code == HTTPStatus.BAD_REQUEST

    def test_user_not_logged(self):
        client.delete('/logout')
        response = client.post('/organization', data = '{"name" : "Taller2"}')
        assert response.status_code == HTTPStatus.UNAUTHORIZED
