from os import environ, path, makedirs
from sendgrid import SendGridAPIClient

class MockAPI(object):
    def send(self, mail):
        file_path = path.join(path.dirname(__file__), '../../mails/mail.txt')
        makedirs(path.dirname(file_path), exist_ok=True)
        file = open(path.abspath(file_path), "w")
        file.write(str(mail))
        file.close()

class Mailer(object):
    def __init__(self):
        if environ['FLASK_ENV'] == 'production':
            self.api = SendGridAPIClient(environ['SENDGRID_KEY'])
        else:
            self.api = MockAPI()

    def send(self, mail):
        self.api.send(mail)
