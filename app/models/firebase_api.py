import firebase_admin
from firebase_admin import credentials
from firebase_admin import messaging
from os import environ

PRODUCTION = 'production'
CREDENTIALS_PATH = 'fbexample-1633e-firebase-adminsdk-q9ga2-2e3cbe2925.json' #example
SENDER = 'sender'
MESSAGE = 'message'
TIMESTAMP = 'timestamp'
ORGANIZATION = 'organization'
CHANNEL = 'channel'

class FirebaseApi(object):
    def __init__(self):
        if environ['FLASK_ENV'] == PRODUCTION:
            cred = credentials.Certificate(CREDENTIALS_PATH)
            self.default_app  = firebase_admin.initialize_app(cred)

    def send_message_to_users(self, list_username, message, organization_name, channel_name):
        if environ['FLASK_ENV'] != PRODUCTION:
            return True
        tokens = get_users_tokens(list_username)
        for token in tokens:
            message = messaging.Message(
                    data = {
        		SENDER : message.sender,
                MESSAGE : message.message,
                TIMESTAMP : message.timestamp,
                ORGANIZATION : organization_name,
                CHANNEL : channel_name,
        		},
        	token = token,
            )
            response = messaging.send(message)
        return True

    def get_users_tokens(list_username):
        tokens = []
        for user in list_username:
            token = user.firebase_token
            if token:
                tokens.append(token)
        return tokens
