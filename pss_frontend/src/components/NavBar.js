import React, { useState } from 'react'
import logo from '../images/logo.svg'
import logoMobile from '../images/logoMobile.svg'
import { AppBar, Box, Button, Drawer, IconButton, Toolbar,Tooltip,Typography } from '@material-ui/core'
import { makeStyles } from '@material-ui/core/styles'
import CustomBtn from './CustomBtn'
import { Link } from 'react-router-dom'
import { green } from '@material-ui/core/colors'
import MenuIcon from "@material-ui/icons/Menu";


const useStyles = makeStyles((theme) => ({
    menuButton: {
      marginRight: theme.spacing(2)
    },
    title: {
      flexGrow: 0,
      textDecoration: 'none'
    },
    buttons:{
        flexGrow: 1
    },
    customColor: {
      // or hex code, this is normal CSS background-color
      backgroundColor: green[500]
    },
    customHeight: {
      minHeight: 200
    },
    offset: theme.mixins.toolbar
  }));


function NavBar() {

        const classes = useStyles();
        const [example, setExample] = useState("primary");
        const isCustomColor = example === "customColor";
        const isCustomHeight = example === "customHeight";
        return (
          <React.Fragment>
            <AppBar
              color={isCustomColor || isCustomHeight ? "secondary" : example}
              className={`${isCustomColor && classes.customColor} ${
                isCustomHeight && classes.customHeight
              }`}
            >
              <Toolbar>
            
                <Typography to="/" component={Link} color="inherit" variant="h6" className={classes.title}>
                  Renato Website
                </Typography>
                <IconButton to="/account" component={Link} color="inherit" className={classes.buttons}>
                  Sobre
                </IconButton>
                <IconButton to="/contact" component={Link} color="inherit" className={classes.buttons}>
                  Contactos
                </IconButton>
              </Toolbar>
            </AppBar>
            <Toolbar />
          </React.Fragment>
  
    )
}

export default NavBar
