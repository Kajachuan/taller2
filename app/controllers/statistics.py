from http import HTTPStatus
from datetime import date, timedelta
from flask import Blueprint, jsonify
from ..models.user import User

statistics = Blueprint('statistics', __name__)

def get_period_statistics(queryset, days):
    period = date.today() - timedelta(days=days)
    queryset = queryset(creation_date__gte=period)
    pipeline = [
        { '$project': {
            'date': { '$dateToString': { 'format': '%Y-%m-%d', 'date': '$creation_date' } }
        } },
        { '$group': {
            '_id': '$date',
            'count' : { '$sum': 1 }
        } }
    ]
    stat = list(queryset.aggregate(*pipeline))
    keys = [dict['_id'] for dict in stat]

    for i in range(0,days+1):
        day = (period + timedelta(days=i)).strftime('%Y-%m-%d')
        if day not in keys:
            stat.append({'_id': day, 'count': 0})

    return sorted(stat, key=lambda dict: dict['_id'])

@statistics.route('/statistics/users', methods=['GET'])
def users_statistics():
    data = {}
    user_queryset = User.objects
    data['count'] = user_queryset.count()
    data['week'] = get_period_statistics(user_queryset, 7)
    data['month'] = get_period_statistics(user_queryset, 30)
    return jsonify(data), HTTPStatus.OK
