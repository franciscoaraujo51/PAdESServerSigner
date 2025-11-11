const drawerWidth = 240;

export default (theme) => ({
    root: {
        display: 'flex',
      },
      appBar: {
        width: '100%',
        backgroundColor: 'primary',
      },
      drawer: {
        width: drawerWidth,
        flexShrink: 0,
        backgroundColor: 'primary',      },
      drawerPaper: {
        width: drawerWidth,
      },
      // necessary for content to be below app bar
      toolbar: theme.mixins.toolbar,
      content: {
        flexGrow: 1,
        backgroundColor: "theme.palette.background.default",
      },
      button: {
        color:"white",
      },
});
