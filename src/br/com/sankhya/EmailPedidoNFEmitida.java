package br.com.sankhya;

import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;

public class EmailPedidoNFEmitida {

	static void NFEmitida() throws SQLException {
		MailJava mj = new MailJava();

		String emailParc="", nomeVend="", emailVend="";
		int ramalVend=0;

		MainClass.mensagem.setLength(0);
		
		//recupera o numero da negociação
        String comando="select nunota, codparc, codvend, codcontato from tgfcab " + 
                "where statusnota='L' and ad_envioemailnotaemitida is null and ad_envioemailpropaprovada = 'SIM'"
               // + "and ad_envioemailsepestoque = 'SIM' "+
                +" and codtipoper in(900,901,912,925,931)and dtalter>='2018-25-05'";
        System.out.println(comando);
        Statement smnt = ConnectMSSQLServer.conn.createStatement(); 
        smnt.execute(comando);
        ResultSet result = smnt.executeQuery(comando);

        while(result.next())
        {

            //Recupera o ramal do vendedor
            if(MainClass.ExecutaComandoNoBanco("SELECT USU.AD_RAMAL FROM TSIUSU USU"
                    + " INNER JOIN TGFVEN VEN ON(VEN.CODVEND=USU.CODVEND)"
                    + " WHERE VEN.CODVEND="+result.getString("codvend"), "select")!=null)
            {
                ramalVend=(Integer) MainClass.ExecutaComandoNoBanco("SELECT USU.AD_RAMAL FROM TSIUSU USU"
                        + " INNER JOIN TGFVEN VEN ON(VEN.CODVEND=USU.CODVEND)"
                        + " WHERE VEN.CODVEND="+result.getString("codvend"), "select");
            }

            //Recupera o email do vendedor
            if(MainClass.ExecutaComandoNoBanco("SELECT USU.EMAIL FROM TSIUSU USU" + 
                    " INNER JOIN TGFVEN VEN ON(VEN.CODVEND=USU.CODVEND)"+  
                    " WHERE VEN.CODVEND="+result.getString("codvend"), "select")!=null)
            {
                emailVend=(String) MainClass.ExecutaComandoNoBanco("SELECT USU.EMAIL FROM TSIUSU USU"+  
                        " INNER JOIN TGFVEN VEN ON(VEN.CODVEND=USU.CODVEND)"+ 
                        " WHERE VEN.CODVEND="+result.getString("codvend"), "select");
            }

            //Recupera o nome do vendedor
            if(MainClass.ExecutaComandoNoBanco("SELECT FUN.NOMEFUNC FROM TGFVEN VEN"+
                    " INNER JOIN TSIUSU USU ON USU.CODVEND = VEN.CODVEND"+
                    " INNER JOIN TFPFUN FUN ON FUN.CODFUNC = USU.CODFUNC AND FUN.CODEMP=3"+
                    " WHERE  USU.CODVEND="+result.getString("codvend"), "select")!=null)
            {
                nomeVend=(String) MainClass.ExecutaComandoNoBanco("SELECT FUN.NOMEFUNC FROM TGFVEN VEN"+
                        " INNER JOIN TSIUSU USU ON USU.CODVEND = VEN.CODVEND"+
                        " INNER JOIN TFPFUN FUN ON FUN.CODFUNC = USU.CODFUNC AND FUN.CODEMP=3"+
                        " WHERE  USU.CODVEND="+result.getString("codvend"), "select");
            }

            //Recupera o email do parceiro
            if(MainClass.ExecutaComandoNoBanco("SELECT CTT.EMAIL FROM TGFCAB CAB"
                    + " INNER JOIN TGFCTT CTT ON CTT.CODCONTATO=CAB.CODCONTATO"
                    + " AND CTT.CODPARC=CAB.CODPARC"
                    + " WHERE CAB.CODCONTATO="+result.getString("codcontato")+" AND CAB.CODPARC="
                    +result.getString("codparc"), "select")!=null)
            {
                emailParc=(String) MainClass.ExecutaComandoNoBanco("SELECT CTT.EMAIL FROM TGFCAB CAB"
                        + " INNER JOIN TGFCTT CTT ON CTT.CODCONTATO=CAB.CODCONTATO"
                        + " AND CTT.CODPARC=CAB.CODPARC"
                        + " WHERE CAB.CODCONTATO="+result.getString("codcontato")+" AND CAB.CODPARC="
                        + result.getString("codparc"), "select");
            }


			//configuracoes de envio
			mj.setSmtpHostMail("email-ssl.com.br");
			mj.setSmtpPortMail("465");
			mj.setSmtpAuth("true");
			mj.setSmtpStarttls("true");
			mj.setUserMail("vendas@medika.com.br");
			mj.setFromNameMail("Equipe de vendas");
			mj.setSmtpAuth("M3dika2017");
		    mj.setPassMail("M3dika2017");
			mj.setCharsetMail("ISO-8859-1");
			mj.setSubjectMail("Pedido "+result.getString("nunota")+". Nota Fiscal Emitida.");
			mj.setBodyMail(htmlMessage(result.getString("nunota"), nomeVend, Integer.toString(ramalVend)));
			mj.setTypeTextMail(MailJava.TYPE_TEXT_HTML);

			//sete quantos destinatarios desejar
			 String dest=emailParc;
			//String dest="adriano.soares@medika.com.br";
             String destCC = "adriano.soares@medika.com.br";

			mj.setToMailsUsers(dest);
			mj.setToCCMailsUsers(destCC);
            
			//Gera o relatorio em PDF
			//GeradorDeRelatorios.geraPdf("\\"+"\\192.168.0.10\\srv-arq\\PUBLICA"+"\\STI"+"\\Adriano"+"\\PEDIDO_DE_VENDA.jrxml", nunota.add(new BigDecimal(122814)));
			//GeradorDeRelatorios.geraPdf("/users/adriano/relatorios/propvenda/PEDIDO_DE_VENDA1x.jrxml", result.getBigDecimal("nunota"));
			GeradorDeRelatorios.geraPdf("C:\\AtualizadoresSankhyaDevExterno\\propostadevenda\\PEDIDO_DE_VENDA1x.jrxml", result.getBigDecimal("nunota"));
			
			//seta quatos anexos desejar
			List files = new ArrayList();
			//files.add("/users/adriano/relatorios/propvenda/Proposta de venda.pdf");
			files.add("C:\\AtualizadoresSankhyaDevExterno\\propostadevenda\\Proposta de venda.pdf");

			mj.setFileMails(files);

			try {
				new MailJavaSender().senderMail(mj);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (MessagingException e) {
				e.printStackTrace();
			}
			
			MainClass.ExecutaComandoNoBanco("Update tgfcab set ad_envioemailnotaemitida='SIM' where nunota="+result.getString("nunota"), "alter");
			
		}
	}

	private static String htmlMessage(String nunota, String nomeVend, String ramalVend) {
		return
				"<html><body style="+"\"font-famaly: verdana; font-size:14px; font-style:bold;"+"\"><b> Prezado(s),<br/><br/>"+		             
				"Pedido N�mero:"+nunota+". Nota fiscal emitida.</b><br/>"+

				"<br/><br/>"+

				"<HR WIDTH=100% style="+"\"border:1px solid #191970;"+
				"\"><img src=https://static.wixstatic.com/media/e2601a_929b52b624db40f99805e415953543b9~mv2.png/v1/fill/w_788,h_349,al_c,usm_0.66_1.00_0.01/e2601a_929b52b624db40f99805e415953543b9~mv2.png><br><br>"+

				"Atenciosamente,"+
				"<br/><br/>"+nomeVend+
				" - Tel:(31) 3688-1901 Ramal:"+ramalVend+" - Equipe de Vendas"+
				"<br><br><HR WIDTH=100% style="+"\"border:1px solid #191970;"+
				"\">"+
				"</body></html>";
	}

	
}
