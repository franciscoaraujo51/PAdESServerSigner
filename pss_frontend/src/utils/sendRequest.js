import axios from 'axios';

export const fetchApi = (method,url,payload = {},headers={},successHandler,errorHandler) => {
     axios({
        method: method,
        url: url,
        data: payload,
        headers: {
            'Content-Type': 'application/json',
            ...headers
          }
      }).then((response) => {
        console.log(response);
        successHandler(response);
      }, (error) => {
        console.log(error);
        errorHandler(error);
      });
}