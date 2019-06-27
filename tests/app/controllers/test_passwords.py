from http import HTTPStatus
from os import path
from taller2.app.app import app

client = app.test_client()
client.post('/register', data='{"username": "testrecovery", "email": "user@test.com",\
                                "password": "password", "password_confirmation": "password"}')
client.post('/register', data='{"username": "testpassword", "email": "user@test.com",\
                                "password": "password", "password_confirmation": "password"}')

def get_token_from_mail():
    file_path = path.join(path.dirname(__file__), '../../../mails/mail.txt')
    file = open(path.abspath(file_path), "r")
    mail = file.read()
    file.close()
    mail = mail.split('<p>')
    token = mail[2].split('</b>')
    token = token[1].strip()
    token = token.split('</p>')[0]
    return token

class TestPasswordsController(object):
    def test_recovery_inexistent_user(self):
        response = client.post('/recovery', data='{"username": "wronguser"}')

        assert response.status_code == HTTPStatus.BAD_REQUEST
        assert response.get_json() == {'message': 'The username does not exist'}

    def test_recovery_correct_user(self):
        response = client.post('/recovery', data='{"username": "testrecovery"}')

        assert response.status_code == HTTPStatus.OK
        assert response.get_json() == {'message': 'The token was sent'}

    def test_new_password_inexistent_user(self):
        response = client.post('/password', data='{"username": "wronguser",\
                                                   "new_password": "newpassword",\
                                                   "new_password_confirmation": "newpassword"}',
                               headers={'Authorization': 'token'})
        assert response.status_code == HTTPStatus.BAD_REQUEST
        assert response.get_json() == {'message': 'The username does not exist'}

    def test_new_password_invalid_token(self):
        response = client.post('/password', data='{"username": "testpassword",\
                                                   "new_password": "newpassword",\
                                                   "new_password_confirmation": "newpassword"}',
                               headers={'Authorization': 'wrongtoken'})
        assert response.status_code == HTTPStatus.BAD_REQUEST
        assert response.get_json() == {'message': 'The token is not valid or it has expired'}

    def test_new_password_wrong_confirmation(self):
        response = client.post('/recovery', data='{"username": "testpassword"}')
        test_token = get_token_from_mail()
        response = client.post('/password', data='{"username": "testpassword",\
                                                   "new_password": "newpassword",\
                                                   "new_password_confirmation": "wrongpassword"}',
                               headers={'Authorization': test_token})
        assert response.status_code == HTTPStatus.BAD_REQUEST
        assert response.get_json() == {'message': 'The password and the confirmation are not the same'}

    def test_new_password_short_password(self):
        response = client.post('/recovery', data='{"username": "testpassword"}')
        test_token = get_token_from_mail()
        response = client.post('/password', data='{"username": "testpassword",\
                                                   "new_password": "pw",\
                                                   "new_password_confirmation": "pw"}',
                               headers={'Authorization': test_token})
        assert response.status_code == HTTPStatus.BAD_REQUEST
        assert response.get_json() == {'message': 'The password is too short. It must have at least five characters'}

    def test_new_password_success(self):
        response = client.post('/recovery', data='{"username": "testpassword"}')
        test_token = get_token_from_mail()
        response = client.post('/password', data='{"username": "testpassword",\
                                                   "new_password": "newpassword",\
                                                   "new_password_confirmation": "newpassword"}',
                               headers={'Authorization': test_token})
        assert response.status_code == HTTPStatus.OK
        assert response.get_json() == {'message': 'The password has been changed'}
