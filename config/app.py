from os import environ

if environ['FLASK_ENV'] == 'production':
    DEBUG = False
    TESTING = False
    SECRET_KEY = environ['SECRET_KEY'].encode()
    MONGODB_SETTINGS = {
        'db': 'hypechat',
        'host': environ['MONGODB_URI']
    }

elif environ['FLASK_ENV'] == 'development':
    DEBUG = True
    TESTING = False
    SECRET_KEY = b'\xe2Y2\x80HM\xf5\xff\n\x11\xe9`k:\xc6\x89'
    MONGODB_SETTINGS = {
        'db': 'hypechat',
        'host': environ['MONGODB_URI']
    }

else:
    DEBUG = False
    TESTING = True
    SECRET_KEY = b'\xe2Y2\x80HM\xf5\xff\n\x11\xe9`k:\xc6\x89'
    MONGODB_SETTINGS = {
        'db': 'hypechat',
        'host': 'mongomock://localhost'
    }
