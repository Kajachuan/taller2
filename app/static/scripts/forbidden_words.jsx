import React from 'react';
import ReactDOM from 'react-dom';
import Menu from './menu'
import MenuButton from './menu';

class ForbiddenWords extends React.Component{
  constructor(props){
    super(props);
    this.state = {
      words: [],
      showForm: false,
      urlAction: "",
      submitText: ""
    };
    this.changeStateAdd = this.changeStateAdd.bind(this);
    this.changeStateDelete = this.changeStateDelete.bind(this);
  }

  changeStateAdd(){
    this.setState({
      showForm: true,
      urlAction: "/admin/forbidden-words/words",
      submitText: "Agregar"
    });
  }

  changeStateDelete(){
    this.setState({
      showForm: true,
      urlAction: "/admin/forbidden-words/word-delete",
      submitText: "Eliminar"
    });
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
    let form;
    if(this.state.showForm){
      form = <form onSubmit={this.handleSubmit} method="post" action={this.state.urlAction}>
                <label>
                  Palabra: <input type="text" name="word" />
                </label>
                <input type="submit" value={this.state.submitText} />
             </form>
    }
    return(
      <div class="list">
        <Menu/>
        <h1>Lista de palabras prohibidas</h1>
        <button onClick={this.changeStateAdd}>Agregar palabra</button>
        <button onClick={this.changeStateDelete}>Eliminar palabra</button>
        {form}
        {listWords}
      </div>);
  }
}

ReactDOM.render(
  <ForbiddenWords/>,
  document.getElementById('content')
);
