package outils;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import javax.swing.JOptionPane;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

import constantes.Erreurs;
import exceptions.SeleniumException;

/**
 * Outils permettant une connection à serveur distant pour l'utilisation du shell.
 * @author levieil_f
 *
 */
public class ShellOutils{
	
	/**
	 * Permet de lancer une commande shell et d'obtenir la réponse de celle-ci.
	 * @param hote l'hote.
	 * @param login l'utilisateur.
	 * @param password le mot de passe.
	 * @param commande la commande à executer.
	 * @return le résultat de l'execution.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public String lancerCommandeShell(String hote, String login, String password, String commande) throws SeleniumException {
		Session session = null;
		Channel channel = null;
		String retour = "ECHEC";
		try{
			JSch jsch = new JSch();
			
			session = jsch.getSession(login, hote, 22);
			session.setPassword(password);

			UserInfo ui = new MyUserInfo(){
				
				public void showMessage(String message){
					//Si un message se presente, on l'ignore mais on le note.
					//System.out.println(message);
				}
				
				public boolean promptYesNo(String message){
					//Si un message se presente, on l'ignore mais on le note.
					//System.out.println(message);
					return true;
				}
			};
			session.setUserInfo(ui);
			// Connection au shell.
			session.connect(30000);
			
			// Ouverture d'un canal de communication avec le distant lancant la commande.
			channel=session.openChannel("exec");
			channel.setInputStream(null);
			ByteArrayOutputStream sortie = new ByteArrayOutputStream();
			channel.setOutputStream(sortie);
			((ChannelExec)channel).setErrStream(System.err);
			((ChannelExec)channel).setCommand(commande);

			channel.connect(3*1000);

			// Permet au code de patienter le temps de l'execution de la commande et de la récupération du resultat.
			while(true){
				if(channel.isClosed()){
					if ( channel.getExitStatus() != 0) {
						// Une erreur est survenue.
						System.out.println(sortie.toString("UTF8"));
						throw new SeleniumException(Erreurs.E018, "Une commande shell n'as pas fonctionnée");
					}
					break;
				}
				try{Thread.sleep(1000);}catch(Exception ee){}
			}
		    
			retour =  sortie.toString("UTF8");
		} catch(JSchException e){
			throw new SeleniumException(Erreurs.E018, "Impossible de se connecter au shell.");
		} catch (UnsupportedEncodingException e) {
			throw new SeleniumException(Erreurs.E018, "Impossible de traiter la sortie du shell.");
		} finally {
			// Une fois que tout est finis , on deconnecte la session et le canal.
		    channel.disconnect();
		    session.disconnect();
		}
		return retour;
	}
	
	/**
	 * Permet d'utiliser la console comme une interface d'utilisation shell.
	 * @param arg
	 */
	public static void main(String[] arg){

		try{
			JSch jsch=new JSch();
			//jsch.setKnownHosts("/home/foo/.ssh/known_hosts");
			// Si l'hote n'est pas connu dans les paramètres du main on utilise un prompt pour demander au user.
			String host=null;
			if(arg.length>0){
				host=arg[0];
			} else {
				host=JOptionPane.showInputDialog("Enter username@hostname", System.getProperty("user.name") + "@localhost");
			}
			
			// On récupère l'utilisateur et le mot de passe pour une connection.
			String user=host.substring(0, host.indexOf('@'));
			host=host.substring(host.indexOf('@')+1);
			Session session=jsch.getSession(user, host, 22);
			String passwd = JOptionPane.showInputDialog("Enter password");
			session.setPassword(passwd);

			// On passe outre la configuration par défaut , en utilisant des prompt java pour demander les choix.
			UserInfo ui = new MyUserInfo(){
				public void showMessage(String message){
					JOptionPane.showMessageDialog(null, message);
				}
				public boolean promptYesNo(String message){
					Object[] options={ "yes", "no" };
					int foo=JOptionPane.showOptionDialog(null,
							message,
							"Warning",
							JOptionPane.DEFAULT_OPTION,
							JOptionPane.WARNING_MESSAGE,
							null, options, options[0]);
					return foo==0;
				}

				// If password is not given before the invocation of Session#connect(),
				// implement also following methods,
				// * UserInfo#getPassword(),
				// * UserInfo#promptPassword(String message) and
				// * UIKeyboardInteractive#promptKeyboardInteractive()

			};
			session.setUserInfo(ui);
			// It must not be recommended, but if you want to skip host-key check,
			// invoke following,
			// session.setConfig("StrictHostKeyChecking", "no");
			//session.connect();
			session.connect(30000); // making a connection with timeout.
			Channel channel=session.openChannel("shell");
			// Enable agent-forwarding.
			//((ChannelShell)channel).setAgentForwarding(true);
			channel.setInputStream(System.in);
			/*
				// a hack for MS-DOS prompt on Windows.
				channel.setInputStream(new FilterInputStream(System.in){
				public int read(byte[] b, int off, int len)throws IOException{
				return in.read(b, off, (len>1024?1024:len));
				}
				});
			 */
			channel.setOutputStream(System.out);
			/*
			// Choose the pty-type "vt102".
			((ChannelShell)channel).setPtyType("vt102");
			// Set environment variable "LANG" as "ja_JP.eucJP".
			((ChannelShell)channel).setEnv("LANG", "ja_JP.eucJP");
			 */
			//channel.connect();
			channel.connect(3*1000);
		} catch(Exception e){
			System.out.println(e);
		}
	}

	/**
	 * Classe définissant une sur-couche de la classe d'informartion utilisateur.
	 * Les critères par défaut sont des validations à non des prompt et une absence de mot de passe.
	 * @author levieil_f
	 *
	 */
	public static abstract class MyUserInfo	implements UserInfo, UIKeyboardInteractive {
		public String getPassword(){ return null; }
		public boolean promptYesNo(String str){ return false; }
		public String getPassphrase(){ return null; }
		public boolean promptPassphrase(String message){ return false; }
		public boolean promptPassword(String message){ return false; }
		public void showMessage(String message){ }
		public String[] promptKeyboardInteractive(String destination,
				String name,
				String instruction,
				String[] prompt,
				boolean[] echo){
			return null;
		}
	}
}

