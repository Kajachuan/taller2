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

    session['current_user'] = user.id
    current_app.logger.info('Logged in')
    return '', HTTPStatus.OK
