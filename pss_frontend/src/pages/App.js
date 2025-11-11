import React, {Component} from 'react';
import Cmd from './cmd/cmd'
import Home from './home/home'
import Cc from './cc/cc'
import styles from './styles';
import {BrowserRouter as Router, Route, Link , Switch} from 'react-router-dom';
import {AppBar, Toolbar,Typography ,Button, withStyles } from '@material-ui/core';
import Drawer from '@material-ui/core/Drawer';
import CssBaseline from '@material-ui/core/CssBaseline';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import CreditCardIcon from '@material-ui/icons/CreditCard';
import WebIcon from '@material-ui/icons/Web';
import { NavLink } from 'react-router-dom';
import { CastConnected } from '@material-ui/icons';
import { Container } from '@material-ui/core';

class App extends Component{
  constructor(props) {
    super(props)
    this.state = { page:1 }
  }


 routes ={
    routes:(
      <Switch>
        <Route path="/" exact strict render={
              () =>{
                return(<Home/>)
              }
            }/>
        <Route path="/cc" exact strict render={
            () =>{
              return(<Cc/>)
            }
          }/>
          <Route path="/cmd" exact strict render={
            () =>{
              return(<Cmd/>)
            }
          }/>
    </Switch>
    )
  }

  handleCCRedirect = () => {
    console.log("olas")
  
    }

   

  render() {
    const { classes } = this.props;

    const ForwardNavLink = React.forwardRef((props, ref) => (
      <NavLink {...props} innerRef={ref} style={{ textDecoration: 'none' }} />
    ));

    return (
      <Router>
        <div className={classes.root}>
        <CssBaseline />
          <AppBar position="fixed" className={classes.appBar}>
            <Toolbar>
              <Button  className={classes.button}  component={ForwardNavLink} to='/' >
                <Typography style={{ marginRight: 250 }} variant="h5" noWrap>
                PAdes server Signer
                </Typography>
              </Button>
              <Button className={classes.button} component={ForwardNavLink} to='/cmd'  >
                <Typography style={{ marginRight: 100 }} variant="h8" noWrap>
                Chave Móvel Digital
                </Typography>
              </Button>
              <Button className={classes.button} component={ForwardNavLink} to='/cc'  >
                <Typography variant="h8" noWrap>
                Cartão de Cidadão
                </Typography>
              </Button>
            </Toolbar>
            
          </AppBar>
          <Container>
          <main className={classes.content}>
              <div className={classes.toolbar}/>
              {this.routes.routes}
              
          </main>
          </Container>

        </div>
      </Router>

    )
  }
}
export default withStyles(styles) (App);
