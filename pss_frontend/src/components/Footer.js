import { Box, Container, Grid } from '@material-ui/core'
import React from 'react'
import { Link } from 'react-router-dom'

function Footer() {
    return (
        <Box px={{xs : 3, sm:10}} py={{xs : 5, sm:10}} bgcolor="text.secondary" color="white">
            <Container maxWidth="lg">
                <Grid container spacing={5}>
                    <Grid item xs={12} sm={4}>
                        <Box borderBottom={1}>
                            Help
                        </Box>
                        <Box>
                            <Link to="/about" style={{ textDecoration: 'none' ,color:"white"}}>Sobre</Link>
                        </Box>
                        <Box>
                            <Link to="/about" style={{ textDecoration: 'none' ,color:"white"}}>Sobre</Link>
                        </Box>
                    </Grid>
                    <Grid item xs={12} sm={4}>
                        <Box borderBottom={1}>
                            Help
                        </Box>
                        <Box>
                            <Link to="/about" style={{ textDecoration: 'none' ,color:"white"}}>Sobre</Link>
                        </Box>
                        <Box>
                            <Link to="/about" style={{ textDecoration: 'none' ,color:"white"}}>Sobre</Link>
                        </Box>
                    </Grid>
                    <Grid item xs={12} sm={4}>
                        <Box borderBottom={1}>
                            Help
                        </Box>
                        <Box>
                            <Link to="/about" style={{ textDecoration: 'none' ,color:"white"}}>Sobre</Link>
                        </Box>
                        <Box>
                            <Link to="/about" style={{ textDecoration: 'none' ,color:"white"}}>Sobre</Link>
                        </Box>
                    </Grid>
                </Grid>
            </Container>
            <Box textAlign="center" pt={{xs : 2, sm:10}} pb={{xs : 5, sm:0}}>
                Renato Website 2022 &reg; 
            </Box>
        </Box>
    )
}

export default Footer
