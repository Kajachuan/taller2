import React from 'react';
import ReactDOM from 'react-dom';
import Menu from './menu'

class ForbiddenWords extends React.Component{
  constructor(props){
    super(props);
    this.state = {
      words: []
    };
  }

  componentDidMount(){
    fetch("/admin/forbidden-words/words")
    .then(res => res.json())
    .then(
      (result) => {
        this.setState({
          words: result.list_of_words
        });
      }
    )
  }

  render(){
    const { words } = this.state;
    return(
    <div>
      <Menu/>
    </div>);
  }
}

ReactDOM.render(
  <ForbiddenWords/>,
  document.getElementById('content')
);
