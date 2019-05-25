import React from 'react';
import ReactDOM from 'react-dom';
import Menu from './menu';
import { LineChart, Line, CartesianGrid, XAxis, YAxis, Tooltip } from 'recharts';

const data = [{name: 'Page A', uv: 400, pv: 2400, amt: 2400},
              {name: 'Page B', uv: 300, pv: 2400, amt: 2400},
              {name: 'Page C', uv: 300, pv: 2400, amt: 2400},
              {name: 'Page D', uv: 200, pv: 2400, amt: 2400},
              {name: 'Page E', uv: 278, pv: 2400, amt: 2400},
              {name: 'Page F', uv: 189, pv: 2400, amt: 2400}];

class Statistics extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      count: 0,
      week: []
    };
  }

  componentDidMount() {
    fetch("/statistics/users")
      .then(res => res.json())
      .then(
        (result) => {
          this.setState({
            count: result.count,
            week: result.week
          });
        }
      )
  }

  render() {
    return(
      <div>
        <Menu/>
        <LineChart width={600} height={300} data={this.state.week} margin={{ top: 5, right: 20, bottom: 5, left: 0 }}>
          <Line type="monotone" dataKey="count" stroke="#8884d8" />
          <CartesianGrid stroke="#ccc" strokeDasharray="5 5" />
          <XAxis dataKey="_id" />
          <YAxis />
          <Tooltip />
        </LineChart>
      <p>El total de usuarios es {this.state.count}</p>
      </div>
    );
  }
}

ReactDOM.render(
  <Statistics/>,
  document.getElementById('content')
);
