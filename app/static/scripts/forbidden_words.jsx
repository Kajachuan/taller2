import React from 'react';
import ReactDOM from 'react-dom';
import Menu from './menu'
import MenuButton from './menu';

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
    const {words} = this.state;
    const listWords = words.map((d) => <li key={d}>{d}</li>);
    return(
      <div class="list">
        <Menu/>
        <h1>Lista de palabras prohibidas</h1>
        <button>Agregar palabra</button>
        <button>Eliminar palabra</button>
        {listWords}
      </div>);
  }
}

ReactDOM.render(
  <ForbiddenWords/>,
  document.getElementById('content')
);
