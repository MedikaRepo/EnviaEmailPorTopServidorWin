package br.com.sankhya;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;

public class EmailPedidoEmSeparacao {

	static void PedidoSeparacao(int nunota, String codVend, String codParc, String codContato) throws SQLException {
		MailJava mj = new MailJava();

		String emailParc="", nomeVend="", emailVend="";
		int ramalVend=0;

		MainClass.mensagem.setLength(0);

		//recupera o numero da negociaÃ§Ã£o
		String comando=
				//Verifica se foi gerada uma tarefa de separação para o pedido em questao
				"SELECT X.NUNOTA, MIN (COD_SITUACAO) AS COD_SITUACAO FROM (  SELECT SXN.NUNOTA,"+
						" ISNULL ((CASE MIN (CASE WHEN SEP.CANCELADA = 'S' THEN 100 WHEN SEP.SITUACAO = 5 THEN 10"+
						" WHEN SEP.SITUACAO = 6 THEN 11 ELSE SEP.SITUACAO END)WHEN 0 THEN 0"+
						" WHEN 1 THEN CASE WHEN EXISTS(SELECT 1 FROM TGWITT I "+
						" WHERE I.NUTAREFA IN (SELECT NUTAREFA FROM TGWSEP SEP2"+
						" INNER JOIN TGWSXN SXN2 ON (SXN2.NUSEPARACAO = SEP2.NUSEPARACAO)"+
						" WHERE SXN2.NUNOTA = SXN.NUNOTA )AND I.SITUACAO IN ('E', 'F'))"+
						" OR EXISTS(SELECT 1 FROM TGWSVAR SVAR INNER JOIN TGWSEP SEPM"+
						" ON (SEPM.NUSEPARACAO =SVAR.NUSEPMAE)"+
						" WHERE SVAR.NUSEPFILHA IN (SELECT NUSEPARACAO FROM TGWSXN SXN3"+ 
						" WHERE SXN3.NUNOTA = SXN.NUNOTA)AND EXISTS(SELECT 1 FROM TGWITT IM"+
						" WHERE IM.NUTAREFA =SEPM.NUTAREFA AND IM.SITUACAO IN('E', 'F')))THEN 2"+
						" ELSE 1 END WHEN 2 THEN 3  WHEN 3 THEN 4 WHEN 4 THEN"+
						" CASE MIN (SEP.STATUSCONF)WHEN 2 THEN 5 WHEN 3 THEN 6 ELSE 12 END WHEN 7 THEN 17"+
						" WHEN 8 THEN(CASE WHEN NOT EXISTS(SELECT 1 FROM TGFITE ITE WHERE ITE.NUNOTA = SXN.NUNOTA"+
						" AND ITE.QTDNEG > ITE.QTDCONFERIDA) THEN 7 ELSE 8 END)WHEN 10 THEN 9 WHEN 11"+
						" THEN 16 WHEN 100 THEN 100 END),-1)AS COD_SITUACAO FROM TGWSXN SXN INNER JOIN TGWSEP SEP"+
						" ON (SXN.NUSEPARACAO = SEP.NUSEPARACAO) GROUP BY SXN.NUNOTA) X  WHERE X.NUNOTA="+nunota+
						" GROUP BY X.NUNOTA";

		System.out.println(comando);
		Statement smnt = ConnectMSSQLServer.conn.createStatement(); 
		smnt.execute(comando);
		try
		{
			ResultSet result = smnt.executeQuery(comando);


			while(result.next())
			{

				System.out.println(result.getString("COD_SITUACAO"));
				if(result.getString("COD_SITUACAO")!="-1")
				{

					//Recupera o ramal do vendedor
					if(MainClass.ExecutaComandoNoBanco("SELECT USU.AD_RAMAL FROM TSIUSU USU"
							+ " INNER JOIN TGFVEN VEN ON(VEN.CODVEND=USU.CODVEND)"
							+ " WHERE VEN.CODVEND="+codVend, "select")!=null)
					{
						ramalVend=(Integer) MainClass.ExecutaComandoNoBanco("SELECT USU.AD_RAMAL FROM TSIUSU USU"
								+ " INNER JOIN TGFVEN VEN ON(VEN.CODVEND=USU.CODVEND)"
								+ " WHERE VEN.CODVEND="+codVend, "select");
					}

					//Recupera o email do vendedor
					if(MainClass.ExecutaComandoNoBanco("SELECT USU.EMAIL FROM TSIUSU USU" + 
							" INNER JOIN TGFVEN VEN ON(VEN.CODVEND=USU.CODVEND)"+  
							" WHERE VEN.CODVEND="+codVend, "select")!=null)
					{
						emailVend=(String) MainClass.ExecutaComandoNoBanco("SELECT USU.EMAIL FROM TSIUSU USU"+  
								" INNER JOIN TGFVEN VEN ON(VEN.CODVEND=USU.CODVEND)"+ 
								" WHERE VEN.CODVEND="+codVend, "select");
					}

					//Recupera o nome do vendedor
					if(MainClass.ExecutaComandoNoBanco("SELECT FUN.NOMEFUNC FROM TGFVEN VEN"+
							" INNER JOIN TSIUSU USU ON USU.CODVEND = VEN.CODVEND"+
							" INNER JOIN TFPFUN FUN ON FUN.CODFUNC = USU.CODFUNC AND FUN.CODEMP=3"+
							" WHERE  USU.CODVEND="+codVend, "select")!=null)
					{
						nomeVend=(String) MainClass.ExecutaComandoNoBanco("SELECT FUN.NOMEFUNC FROM TGFVEN VEN"+
								" INNER JOIN TSIUSU USU ON USU.CODVEND = VEN.CODVEND"+
								" INNER JOIN TFPFUN FUN ON FUN.CODFUNC = USU.CODFUNC AND FUN.CODEMP=3"+
								" WHERE  USU.CODVEND="+codVend, "select");
					}

					//Recupera o email do parceiro
					if(MainClass.ExecutaComandoNoBanco("SELECT CTT.EMAIL FROM TGFCAB CAB"
							+ " INNER JOIN TGFCTT CTT ON CTT.CODCONTATO=CAB.CODCONTATO"
							+ " AND CTT.CODPARC=CAB.CODPARC"
							+ " WHERE CAB.CODCONTATO="+codContato+" AND CAB.CODPARC="
							+codParc, "select")!=null)
					{
						emailParc=(String) MainClass.ExecutaComandoNoBanco("SELECT CTT.EMAIL FROM TGFCAB CAB"
								+ " INNER JOIN TGFCTT CTT ON CTT.CODCONTATO=CAB.CODCONTATO"
								+ " AND CTT.CODPARC=CAB.CODPARC"
								+ " WHERE CAB.CODCONTATO="+codContato+" AND CAB.CODPARC="
								+ codParc, "select");
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
					mj.setSubjectMail("Pedido "+nunota+". Em separação.");
					mj.setBodyMail(htmlMessage(String.valueOf(nunota), nomeVend, Integer.toString(ramalVend)));
					mj.setTypeTextMail(MailJava.TYPE_TEXT_HTML);

					//sete quantos destinatarios desejar
					 String dest=emailParc;
		             String destCC = "adriano.soares@medika.com.br"; 
					
		            mj.setToMailsUsers(dest);
		            mj.setToCCMailsUsers(destCC);  

					//Gera o relatorio em PDF
					//GeradorDeRelatorios.geraPdf("\\"+"\\192.168.0.10\\srv-arq\\PUBLICA"+"\\STI"+"\\Adriano"+"\\PEDIDO_DE_VENDA.jrxml", nunota.add(new BigDecimal(122814)));
					BigDecimal bigNunota=new BigDecimal(nunota);
					//GeradorDeRelatorios.geraPdf("/users/adriano/relatorios/propvenda/PEDIDO_DE_VENDA1x.jrxml", bigNunota);
					GeradorDeRelatorios.geraPdf("C:\\AtualizadoresSankhyaDevExterno\\propostadevenda\\PEDIDO_DE_VENDA1x.jrxml", bigNunota);

					//seta quatos anexos desejar
					List files = new ArrayList();
					files.add("C:\\AtualizadoresSankhyaDevExterno\\propostadevenda\\Proposta de venda.pdf");

					mj.setFileMails(files);

					try {
						new MailJavaSender().senderMail(mj);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					} catch (MessagingException e) {
						e.printStackTrace();
					}
					
					MainClass.ExecutaComandoNoBanco("Update tgfcab set ad_envioemailsepestoque='SIM' where nunota="+nunota, "alter");
				}
			}
		}catch (Exception e) {
			System.out.println("Nao retornou nada.");
		}
	}

	private static String htmlMessage(String nunota, String nomeVend, String ramalVend) {
		return
				"<html><body style="+"\"font-famaly: verdana; font-size:14px; font-style:bold;"+"\"><b> Prezado(s),<br/><br/>"+		             
				"Pedido Número:"+nunota+" está em fase de separação.</b><br/>"+

				"<br/><br/>"+

				"<HR WIDTH=100% style="+"\"border:1px solid #191970;"+
				"\"><img src=https://static.wixstatic.com/media/e758ec_3728c5aec940473f8f6b208034aea779~mv2.png/v1/fill/w_600,h_350,al_c,usm_0.66_1.00_0.01/e758ec_3728c5aec940473f8f6b208034aea779~mv2.png><br><br>"+

				"Atenciosamente,"+
				"<br/><br/>"+nomeVend+
				" - Tel:(31) 3688-1901 Ramal:"+ramalVend+" - Equipe de Vendas"+
				"<br><br><HR WIDTH=100% style="+"\"border:1px solid #191970;"+
				"\">"+
				"</body></html>";
	}


}
