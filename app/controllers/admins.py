from http import HTTPStatus
from flask import Blueprint, request, abort, session, current_app, render_template
from ..models.admin import Admin

admins = Blueprint('admins', __name__)

@admins.route('/admin/', methods=['GET', 'POST'])
def admin_login():
    if request.method == 'GET':
        return render_template('index.html')

    data = request.get_json(force=True)
    name = data['name']
    current_app.logger.debug('The name is: ' + name)
    password = data['password']

    admin = Admin.authenticate(name, password)
    if not admin:
        current_app.logger.info('The name or password are wrong')
        abort(HTTPStatus.BAD_REQUEST)

    session['admin'] = admin.name
    current_app.logger.info('The admin ' + name + ' is logged in')
    return '', HTTPStatus.OK

@admins.route('/admin/logout/', methods=['DELETE'])
def admin_logout():
    name = session.pop('admin')
    current_app.logger.info('The admin ' + name + ' was logged out')
    return '', HTTPStatus.OK
