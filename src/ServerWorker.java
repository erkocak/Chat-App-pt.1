import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

public class ServerWorker extends Thread{
    private final Server server;
    private final Socket clientSocket;
    private String login = null;
    private OutputStream outputStream;

    public ServerWorker(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    public String getLogin(){
        return this.login;
    }
    
    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void handleClientSocket() throws IOException, InterruptedException {
        InputStream inputStream = this.clientSocket.getInputStream();
        this.outputStream = this.clientSocket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while((line = reader.readLine()) != null) {
            String[] tokens = line.split(" ");
            if (tokens != null & tokens.length > 0){
                String cmd = tokens[0];
                if("quit".equalsIgnoreCase(cmd) || "logoff".equalsIgnoreCase(cmd)){
                    handleLogoff();
                    break;
                }
                else if ("login".equalsIgnoreCase(cmd)){
                    handleLogin(outputStream, tokens);
                }
                else{
                    String msg = "Unknown " + cmd + "\n";
                    outputStream.write(msg.getBytes()); 
                }  
            }
        }

        this.clientSocket.close();
    }

    private void handleLogoff() throws IOException {
        server.removeWorker(this);

        List<ServerWorker> workerList = server.getWorkerList();
        String offlineMsg = "Offline " + login + "\n"; 
        for(ServerWorker worker : workerList){
            if(!login.equals(worker.getLogin())){
                worker.send(offlineMsg);
            }    
        }
        clientSocket.close();
    }

    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException{
        if(tokens.length == 3){
            String login = tokens[1];
            String password = tokens[2];

            if(login.equals("guest") && password.equals("guest") || (login.equals("jim") && password.equals("jim"))){
                String msg = "ok login\n";
                outputStream.write(msg.getBytes());
                
                this.login = login;
                System.out.println("User logged in succesfully: " + login);

                List<ServerWorker> workerList = server.getWorkerList();
                
                //Send current user all other online logins:
                for(ServerWorker worker : workerList){
                    if(worker.getLogin() != null){
                        if(!login.equals(worker.getLogin())){
                            String msg2 = "Already online " + worker.getLogin() + "\n";
                            send(msg2);
                        }    
                    }
                }

                //Send other online users current user's status (login):
                String onlineMsg = "Online " + login + "\n"; 
                for(ServerWorker worker : workerList){
                    if(!login.equals(worker.getLogin())){
                        worker.send(onlineMsg);
                    }    
                }
            }
            else{
                String msg = "error login\n";
                outputStream.write(msg.getBytes());
            }
        }
    }

    private void send(String msg) throws IOException {
        if(login != null){
            outputStream.write(msg.getBytes());
        }
    }
}