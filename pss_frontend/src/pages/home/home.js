import React, { Component, Fragment } from 'react';
import styles from './styles';
import { withStyles } from '@material-ui/core/styles';
import { Typography } from '@material-ui/core';
import { makeStyles } from '@material-ui/core/styles';
import SecurityIcon from '@material-ui/icons/Security';
import EventNoteIcon from '@material-ui/icons/EventNote';
import TrendingUpIcon from '@material-ui/icons/TrendingUp';
import Grid from './../../components/Grid';
import IconButton from '@material-ui/core/IconButton';
import CreditCardIcon from '@mui/icons-material/CreditCard';
import { Link } from 'react-router-dom';
import CloudIcon from '@mui/icons-material/Cloud';

class Home extends Component {
  constructor(props) {
    super(props);
    }

    //Classes estilisticas para este componente
    classes = makeStyles((theme) => ({
        wrapper: {
            width: "65%",
            margin: "auto",
            textAlign: "center"
          },
          bigSpace: {
            marginTop: "5rem"
          },
          littleSpace:{
            marginTop: "2.5rem",
          },
          grid:{
            display: "flex", 
            justifyContent: "center",
            alignItems: "center",
            flexWrap: "wrap", 
          }
    }))
    

  render() {
    return (
    <div>
    <div className={this.classes.wrapper}>
      <Typography variant="h4" className={this.classes.bigSpace} color="primary">
       
      </Typography>
      <Typography variant="h5" className={this.classes.littleSpace} color="primary">
       Escolha uma das opções para realizar a assinatura:
       </Typography>
    </div>
    <div className={`${this.classes.grid} ${this.classes.bigSpace}`}>


    <Grid icon={<IconButton component={Link} to="/cmd" color="primary"> <CloudIcon style={{fill: "#4360A6", height:"125", width:"125"}}/> </IconButton>} to="/" title="Chave Móvel Digital" btnTitle="Show me More" />
    <Grid icon={<IconButton component={Link} to="/cc" color="primary"><CreditCardIcon style={{fill: "#449A76", height:"125", width:"125"}}/></IconButton>} title="Cartão de Cidadão" btnTitle="Show me More"/>
  </div>

  </div>

    )
  }

}
export default withStyles(styles)(Home);