import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

//Classe iniciada um uma nova Thread
public class Servidor extends Thread{
    private static ArrayList<BufferedWriter>usuarios;
    private static ServerSocket servidor;
    private String nome;
    private Socket con;
    private InputStream is;
    private InputStreamReader isr;
    private BufferedReader bfr;

   //Construtor, recebe o parametro do tipo Socket
    public Servidor(Socket con){
        this.con = con;
        try {
            //Estabelece a conexão e le as mensagens enviada pelo cliente
            is  = con.getInputStream();
            isr = new InputStreamReader(is);
            bfr = new BufferedReader(isr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Método Inicio
    public void run(){
        System.out.println("chegou");
        try{
            //Verifica as mensagens para serem enviadas ao client side
            String msg;
            OutputStream ou =  this.con.getOutputStream();
            Writer ouw = new OutputStreamWriter(ou);
            BufferedWriter bfw = new BufferedWriter(ouw);
            usuarios.add(bfw);
            nome = msg = bfr.readLine();

            //Loop para verificar se existem novas mensagens, caso exista vai para o metodo enviaTodos
            while(!"Sair".equalsIgnoreCase(msg) && msg != null)
            {
                msg = bfr.readLine();
                enviaTodos(bfw, msg);
                System.out.println(msg);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }


     // Método usado para enviar mensagem para todos os clientes
    public void enviaTodos(BufferedWriter bwSaida, String msg) throws  IOException
    {
        BufferedWriter bwS;
        //Percorre a lista de clientes e manda uma cópia da mensagem para cada um
        for(BufferedWriter bw : usuarios){
            bwS = bw;
            if(!(bwSaida == bwS)){
                bw.write(nome + " -> " + msg+"\r\n");
                bw.flush();
            }
        }
    }

    //Método Main
    public static void main(String []args) {
        try{
            //Cria os objetos necessários para instânciar o servidor
            JLabel lblMensagem = new JLabel("Porta do Servidor:");
            JTextField txtPorta = new JTextField("4000");
            Object[] texts = {lblMensagem, txtPorta };
            JOptionPane.showMessageDialog(null, texts);
            servidor = new ServerSocket(Integer.parseInt(txtPorta.getText()));
            usuarios = new ArrayList<BufferedWriter>();
            JOptionPane.showMessageDialog(null,"Servidor ativo na porta: "+ txtPorta.getText());

            //Esperando conexão, ao estabelecer, inicia uma nova thread no servidor
            while(true){
                System.out.println("Aguardando conexão...");
                Socket con = servidor.accept();
                System.out.println("Cliente conectado...");
                Thread t = new Servidor(con);
                t.start();
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

}
