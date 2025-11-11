import React, {Component} from 'react';
import styles from './styles';
import {fetchApi} from '../../utils/sendRequest'
import {Grid, TextField, withStyles } from '@material-ui/core';

import { makeStyles } from '@material-ui/core/styles';
import Stepper from '@material-ui/core/Stepper';
import Step from '@material-ui/core/Step';
import StepLabel from '@material-ui/core/StepLabel';
import StepContent from '@material-ui/core/StepContent';
import Button from '@material-ui/core/Button';
import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';
import { Container, Card } from '@material-ui/core';
import MenuItem from '@material-ui/core/MenuItem';
import FormControl from '@material-ui/core/FormControl';
import Select from '@material-ui/core/Select';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import RadioGroup from '@material-ui/core/RadioGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Radio from '@material-ui/core/Radio';

class Cc extends Component{
  constructor(props) {
    super(props)
    this.state = { 
      date: new Date(),
      selectedFile : '',
      assinatura: 'pades_t',
      numeroFicheiros: 0,
      nrTelemovel:'+351 912123123',
      docName:'PADESDSSTESTER',
      pin:'',
      base64:'',
      token:'',
      credentialId:'',
      otp:'',
      activeStep : 0,
      visivel:"nao",
      steps : ['Insira os seguintes dados','Selecione o tipo de assinatura', 'Selecione o ficheiro', 'Assinatura visível'],
      openOTP:false,
      openDialogVisivel:false,
      selectedImage:'',
      xx:'0',
      yy:'0',
      width:'0',
      height:'0',
      sad:'',
      hash:'',
      base64Image:''
    }
   
    this.handleInputFile = this.handleInputFile.bind(this);
    this.handleInputChangeText = this.handleInputChangeText.bind(this);
    this.handleSendSigningRequest = this.handleSendSigningRequest.bind(this);
    this.handleSendOtp = this.handleSendOtp.bind(this);
}
//tipos de assinatura
tiposAssinatura = ["pades_b","pades_t","pades_lt","pades_lta"]

//Classes estilisticas para este componente
classes = makeStyles((theme) => ({
  root: {
    width: '100%',
  },
  button: {
    marginTop: "20",
    marginRight: theme.spacing(2),
  },
  actionsContainer: {
    marginBottom: theme.spacing(2),
  },
  resetContainer: {
    padding: theme.spacing(3),
  },
}))

// Abrir o dialog para por o otp
handleClickOpen = () => {
  this.setState({ openOTP: true });
};

// Fechar o dialog
handleClose = () => {
  this.setState({ openOTP: false });
};

// Fechar o dialog da assinatura visivel
handleClose2 = () => {
  this.setState({ openDialogVisivel: false });
};

handleConfirmVisible = () =>{
  this.setState({openDialogVisivel:false})
  this.setState({openOTP:true})
}

// Avançar um step no pipeline 
handleNext = () => {
  this.setState({ activeStep: this.state.activeStep+1 });
};

// Recuar um step no pipeline
handleBack = () => {
  this.setState({ activeStep: this.state.activeStep-1 });
};

// Recomeçar o pipeline
handleReset = () => {
  this.setState({ activeStep: 0 });
};

visivel_handleChange = (event) => {
  this.setState({visivel:event.target.value});
};


componentWillMount() {
  console.log(window);
}

/**
   * When the user change input form.
   * @param event
   */
  handleInputChangeText = (event) => {
    const input = event.target;
    const { value } = input;

    this.setState({
      [input.name]: value
    });
  };

  handleInputFile = (event) =>{
    var numeroFicheiros = this.state.numeroFicheiros+1;
    let file = event.target.files[0];
    let reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onloadend = () => {
      this.setState({
        selectedFile: file,
        numeroFicheiros: numeroFicheiros,
        base64: reader.result
      });
    };
  }

  handleInputChangeXX = (event) => {
    const input = event.target;
    const { value } = input;

    this.setState({
      xx: value
    });
  };

  handleInputChangeYY = (event) => {
    const input = event.target;
    const { value } = input;

    this.setState({
      yy: value
    });
  };

  //Input imagem para assinatura
  handleInputImage = (event) =>{
   // var numeroFicheiros = this.state.numeroFicheiros+1;
    let file = event.target.files[0];
    let reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onloadend = () => {
      this.setState({
        selectedImage: file,
       // numeroFicheiros: numeroFicheiros,
       base64Image: reader.result
      });
    };
  }


  handleSendSigningRequest = (event) =>{
    let fileBase64 = this.state.base64.slice(28,);

    /*  
      * Isto depois tens de passar (a proxima linha) só para quando o fetch executar com sucesso.
      * 
    */
    if(this.state.visivel == "sim"){
      this.setState({ openDialogVisivel: true });
      return;
    }else{
      this.setState({ openOTP: false });
    }


    fetchApi(
      'post',
      'http://localhost:8080/cc/auth/login',
      {
        phoneNumber:this.state.nrTelemovel,
        pin:this.state.pin
      },
      {},
      (res) => {
        console.log(res.data.access_token);
        this.setState({
          token:res.data.access_token
        })
        fetchApi(
          'get',
          'http://localhost:8080/cc/credentials/list',
          {},
          {
            Authorization:'Bearer '+this.state.token
          },
          (res) => {
            console.log(res.data.token);
            this.setState({
              credentialId:res.data.credentialID
            })

            fetchApi(
              'post',
              'http://localhost:8082/plugin/getCertificates',
              {
                credentialID: this.state.credentialId[0],
                access_token: 'Bearer '+this.state.token
              },
              {},
              (res) => {
                console.log(res.data);
                
                fetchApi(
                  'post',
                  'http://localhost:8080/cc/dssFiler2Sign',
                  {
                    signatureLevel:this.state.assinatura,
                    credentialID:this.state.credentialId[0],
                    docName:this.state.docName,
                    file:fileBase64,
                    reason:"Web Signature",
                    location:"Braga",
                    contactInfo:this.state.nrTelemovel
                  },
                  {
                    Authorization:'Bearer '+this.state.token
                  },
                  (res) => {
                    console.log(res.data.token);
                    this.setState({
                      sad:res.data.sad,
                      hash:res.data.hash
                    })


                    fetchApi(
                      'post',
                      'http://localhost:8082/plugin/sign',
                      {
                        credentialID: this.state.credentialId[0],
                        "access_token": 'Bearer '+this.state.token,
                        "sad":this.state.sad,
                        "pinCode":this.state.pin,
                        "hash":this.state.hash
                      },
                      {},
                      (res) => {
                        console.log(res.data);
                        fetchApi(
                          'post',
                          'http://localhost:8080/cc/dssFiler2SendOTP',
                          {
                            credentialId:this.state.credentialId[0],
                            sad:this.state.sad,
                            otp:"123456"
                          },
                          {
                            Authorization:'Bearer '+this.state.token
                          },
                          (res) => {
                            //const filename =  res.headers.get('Content-Disposition').split('filename=')[1];
                            var data = new Blob([res].data, {type: 'application/pdf'});
                            var csvURL = window.URL.createObjectURL(data);
                            var tempLink = document.createElement('a');
                            tempLink.href = csvURL;
                            tempLink.setAttribute('download', 'filename.pdf');
                            tempLink.click();
                            this.setState({ openOTP: false });
                            console.log(res.data);
                          },
                          () => {
                              console.log("nao deu");
                          }
                        )
                      },
                      () => {
                          console.log("nao deu");
                      }
                    )
                    
                  },
                  () => {
                      console.log("nao deu");
                  }
                )

              },
              () => {
                  console.log("nao deu");
              }
            )
          },
          () => {
              console.log("nao deu");
          }
        )
      },
      () => {
          console.log("nao deu");
      }
    )

    this.setState({ activeStep: this.state.activeStep+1 })
  }

  handleSendSigningRequestImage = (event) =>{
    let fileBase64 = this.state.base64.slice(28,);
    let imagebase64 = this.state.base64Image.slice(21,);
    this.setState({openDialogVisivel:false})
 


    fetchApi(
      'post',
      'http://localhost:8080/cc/auth/login',
      {
        phoneNumber:this.state.nrTelemovel,
        pin:this.state.pin
      },
      {},
      (res) => {
        console.log(res.data.access_token);
        this.setState({
          token:res.data.access_token
        })
        fetchApi(
          'get',
          'http://localhost:8080/cc/credentials/list',
          {},
          {
            Authorization:'Bearer '+this.state.token
          },
          (res) => {
            console.log(res.data.token);
            this.setState({
              credentialId:res.data.credentialID
            })

            fetchApi(
              'post',
              'http://localhost:8082/plugin/getCertificates',
              {
                credentialID: this.state.credentialId[0],
                access_token: 'Bearer '+this.state.token
              },
              {},
              (res) => {
                console.log(res.data);
                
                fetchApi(
                  'post',
                  'http://localhost:8080/cc/dssFiler2Sign',
                  {
                    signatureLevel:this.state.assinatura,
                    credentialID:this.state.credentialId[0],
                    docName:this.state.docName,
                    file:fileBase64,
                    reason:"Web Signature",
                    location:"Braga",
                    contactInfo:this.state.nrTelemovel,
                    imageText:"Francisco Araujo /n assinatura",
                    imageParameters:{
                      image:imagebase64,
                      xAxis:this.state.xx,
                      yAxis:this.state.yy,
                      width:this.state.width,
                      height:this.state.height
                      }
                  },
                  {
                    Authorization:'Bearer '+this.state.token
                  },
                  (res) => {
                    console.log(res.data.token);
                    this.setState({
                      sad:res.data.sad,
                      hash:res.data.hash
                    })


                    fetchApi(
                      'post',
                      'http://localhost:8082/plugin/sign',
                      {
                        credentialID: this.state.credentialId[0],
                        "access_token": 'Bearer '+this.state.token,
                        "sad":this.state.sad,
                        "pinCode":this.state.pin,
                        "hash":this.state.hash
                      },
                      {},
                      (res) => {
                        console.log(res.data);
                        fetchApi(
                          'post',
                          'http://localhost:8080/cc/dssFiler2SendOTP',
                          {
                            credentialId:this.state.credentialId[0],
                            sad:this.state.sad,
                            otp:"123456"
                          },
                          {
                            Authorization:'Bearer '+this.state.token
                          },
                          (res) => {
                            //const filename =  res.headers.get('Content-Disposition').split('filename=')[1];
                            var data = new Blob([res].data, {type: 'application/pdf'});
                            var csvURL = window.URL.createObjectURL(data);
                            var tempLink = document.createElement('a');
                            tempLink.href = csvURL;
                            tempLink.setAttribute('download', 'filename.pdf');
                            tempLink.click();
                            this.setState({ openOTP: false });
                            console.log(res.data);
                          },
                          () => {
                              console.log("nao deu");
                          }
                        )
                      },
                      () => {
                          console.log("nao deu");
                      }
                    )
                    
                  },
                  () => {
                      console.log("nao deu");
                  }
                )

              },
              () => {
                  console.log("nao deu");
              }
            )
          },
          () => {
              console.log("nao deu");
          }
        )
      },
      () => {
          console.log("nao deu");
      }
    )

    this.setState({ activeStep: this.state.activeStep+1 })
  }



  

  handleSendOtp = (event) =>{
    let bearerToken = 'Bearer '+this.state.token;
    fetchApi(
      'post',
      'http://localhost:8080/cc/dssFiler2SendOTP',
      {
        otp:this.state.otp,
        credentialId:this.state.credentialId,
        sad:this.state.sad
      },
      {
        Authorization:bearerToken
      },
      (res) => {
        //const filename =  res.headers.get('Content-Disposition').split('filename=')[1];
        var data = new Blob([res].data, {type: 'application/pdf'});
        var csvURL = window.URL.createObjectURL(data);
        var tempLink = document.createElement('a');
        tempLink.href = csvURL;
        tempLink.setAttribute('download', 'filename.pdf');
        tempLink.click();
        this.setState({ openOTP: false });
        console.log(res.data);
      },
      () => {
          console.log("nao deu");
      }
    )
  }


//Função com o conteúdo de cada Step do pipeline
getStepContent(step) {
  switch (step) {
    case 0:
      return (
        <Container>
          <Grid
            container
            direction="column"
            justify="flex-start"
            alignItems="flex-start"
          >
            <TextField id="nrTelemovel" value={this.state.nrTelemovel} name="nrTelemovel" onChange={this.handleInputChangeText} label="Contacto telefónico" />
            <TextField id="pin" value={this.state.pin} name="pin" onChange={this.handleInputChangeText}  label="PIN" />
          </Grid>
        </Container>
      );
      
    case 1:
      return (
        <Grid xs={8}>
          <FormControl className={this.classes.formControl}>
            <Select
              labelId="demo-simple-select-label"
              id="demo-simple-select"
              value={this.state.assinatura}
              name="assinatura"
              onChange={this.handleInputChangeText}
            >
            {this.tiposAssinatura.map(tipo => (
                  <MenuItem value={tipo}>{tipo}</MenuItem>
              ))}
            </Select>
          </FormControl>
        </Grid>
        );
    case 2:
      return (
        <Container>
          <Grid
            container
            direction="column"
            justify="space-between"
            alignItems="flex-start"

          >
            <TextField id="docName" value={this.state.docName} name="docName" onChange={this.handleInputChangeText} label="Nome do documento" />
          </Grid>
          <Grid
            container
            direction="column"
            justify="space-between"
            alignItems="flex-start"

          >
            <Button variant="contained" component="label">
                Upload File
                <input
                  onChange={this.handleInputFile}
                  type="file"
                  style={{ display: "none" }}
                />
            </Button>
            {this.state.numeroFicheiros === 0 ? <Typography>Nenhum ficheiro selecionado</Typography> : <Typography>{this.state.selectedFile.name}</Typography>}
            </Grid>
        </Container>
      );
    case 3:
      return (
        <FormControl component="fieldset">
        <RadioGroup aria-label="Visivel" name="Visivel" value={this.state.visivel} onChange={this.visivel_handleChange}>
          <FormControlLabel value={"sim"} control={<Radio />} label="Sim" />
          <FormControlLabel value={"nao"} control={<Radio />} label="Não" />
        </RadioGroup>
      </FormControl>
        );
    default:
      return 'Unknown step';
  }
}

render() {
  return (
    <Grid xs={12}>
      <Card>
        <Stepper activeStep={this.state.activeStep} orientation="vertical">
          {this.state.steps.map((label, index) => (
            <Step key={label}>
              <StepLabel>{label}</StepLabel>
              <StepContent>
                <Grid
                  container
                  direction="column"
                  justify="space-between"
                  alignItems="flex-start"
                >
                  <Typography>{this.getStepContent(index)}</Typography>
                  <Container>
                    <Button
                      disabled={this.state.activeStep === 0}
                      onClick={this.handleBack}
                      className={this.classes.button}
                    >
                    Anterior
                    </Button>
                    {this.state.activeStep === this.state.steps.length - 1 ? 
                    (
                       <Button
                       variant="contained"
                       color="primary"
                       onClick={this.handleSendSigningRequest}
                       className={this.classes.button}
                     >
                       Assinar
                       </Button>
                    ) : 
                    ( <Button
                      variant="contained"
                      color="primary"
                      onClick={this.handleNext}
                      className={this.classes.button}
                    >               
                      Próximo
                      </Button>
                    )}

                  </Container>
                </Grid>
              </StepContent>
            </Step>
          ))}
        </Stepper>
        {this.state.activeStep === this.state.steps.length && (
          <Paper square elevation={0} className={this.classes.resetContainer}>
            <Button onClick={this.handleReset} variant="contained"
                      color="primary"className={this.classes.button}>
              Recomeçar
            </Button>
          </Paper>
        )}
      </Card>
      <Dialog open={this.state.openOTP} onClose={this.handleClose} aria-labelledby="form-dialog-title">
        <DialogTitle id="form-dialog-title">OTP</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Insira o código que recebeu no seu dispositivo movel.
          </DialogContentText>
          <TextField
            autoFocus
            value={this.state.otp}
            id="otp"
            label="OTP"
            fullWidth
            name="otp"
            onChange={this.handleInputChangeText}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={this.handleClose} color="primary">
            Cancelar
          </Button>
          <Button onClick={this.handleSendOtp} color="primary">
            Confirmar
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={this.state.openDialogVisivel} onClose={this.handleClose2} aria-labelledby="form-dialog-title">
        <DialogTitle id="form-dialog-title2">Assinatura visível</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Insira a imagem e a posição que deseja para a sua assinatura.
          </DialogContentText>
          <Button variant="contained" component="label">
                Upload File
                <input
                  onChange={this.handleInputImage}
                  type="file"
                  style={{ display: "none" }}
                />
          </Button>
          <Typography>Posição</Typography>
          <Grid
            container
            direction="column"
            justify="space-between"
            alignItems="flex-start"

          >
          <TextField
            autoFocus
            value={this.state.xx}
            id="xx"
            label="XX:"
            fullWidth
            name="xx"
            onChange={this.handleInputChangeText}
          />
          <TextField
            autoFocus
            value={this.state.yy}
            id="yy"
            label="YY:"
            fullWidth
            name="yy"
            onChange={this.handleInputChangeText}
          />
          <TextField
            autoFocus
            value={this.state.width}
            id="width"
            label="width:"
            fullWidth
            name="width"
            onChange={this.handleInputChangeText}
          />
          <TextField
            autoFocus
            value={this.state.height}
            id="height"
            label="height:"
            fullWidth
            name="height"
            onChange={this.handleInputChangeText}
          />
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={this.handleClose2} color="primary">
            Cancelar
          </Button>
          <Button onClick={this.handleSendSigningRequestImage} color="primary">
            Confirmar
          </Button>
        </DialogActions>
      </Dialog>
      </Grid>
  )
}
}
export default withStyles(styles) (Cc);