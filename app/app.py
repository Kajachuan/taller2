from flask import Flask, render_template
from flask_mongoengine import MongoEngine
from os import environ

app = Flask(__name__)
app.config.from_pyfile('../config/app.py')
db = MongoEngine(app)

try:
    from .controllers.users import users
    from .controllers.sessions import sessions
except:
    from controllers.users import users
    from controllers.sessions import sessions

app.register_blueprint(users)
app.register_blueprint(sessions)

@app.route('/')
def root():
    return render_template('index.html')
