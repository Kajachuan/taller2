from http import HTTPStatus
from flask import Blueprint, request, session, current_app, render_template, jsonify, redirect, url_for, flash
from ..models.admin import Admin

admins = Blueprint('admins', __name__)

@admins.route('/admin/', methods=['GET', 'POST'])
def admin_login():
    if request.method == 'GET':
        return render_template('login.html')

    name = request.form['name']
    current_app.logger.debug('The name is: ' + name)
    password = request.form['password']

    admin = Admin.authenticate(name, password)
    if not admin:
        current_app.logger.info('The name or password are wrong')
        flash('El nombre y/o la contraseña son incorrectos')
        return redirect(url_for('admins.admin_login'))

    session['admin'] = admin.name
    current_app.logger.info('The admin ' + name + ' is logged in')
    return jsonify(message='The admin ' + name + ' is logged in'), HTTPStatus.OK

@admins.route('/admin/logout/', methods=['DELETE'])
def admin_logout():
    name = session.pop('admin')
    current_app.logger.info('The admin ' + name + ' was logged out')
    return jsonify(message='The admin ' + name + ' was logged out'), HTTPStatus.OK
