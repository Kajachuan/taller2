from http import HTTPStatus
from flask import Blueprint, request, abort, session, current_app
from ..models.user import User

sessions = Blueprint('sessions', __name__)

@sessions.route('/login', methods=['POST'])
def login():
    data = request.get_json(force=True)
    username = data['username']
    current_app.logger.debug('The username is: ' + username)
    password = data['password']

    user = User.authenticate(username, password)
    if not user:
        current_app.logger.info('The username or password are wrong')
        abort(HTTPStatus.BAD_REQUEST)

    session['username'] = user.username
    current_app.logger.info('The user ' + username + ' is logged in')
    return '', HTTPStatus.OK

@sessions.route('/logout', methods=['DELETE'])
def logout():
    username = session.pop('username')
    current_app.logger.info('The user ' + username + ' was logged out')
    return '', HTTPStatus.OK
