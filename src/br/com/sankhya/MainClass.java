package br.com.sankhya;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MainClass
{

	static StringBuffer mensagem = new StringBuffer();
	static BigDecimal nunota=BigDecimal.ZERO;

	public static Object ExecutaComandoNoBanco(String sql, String op)
	{
		try
		{
			Statement smnt = ConnectMSSQLServer.conn.createStatement(); 

			if(op=="select")
			{
				smnt.execute(sql);
				ResultSet result = smnt.executeQuery(sql); 
				result.next();
				return result.getObject(1);
			}
			else if(op=="alter")
			{
				smnt.executeUpdate(sql);
				return (Object)1;
			}
			else
			{
				return null;
			}
		}
		catch(SQLException ex)
		{
			System.err.println("SQLException: " + ex.getMessage());
			mensagem.append("Erro ao obter campo SQL("+ex.getMessage()+") \n");
			return null;
		}
	}

	public static void main(String[] args) throws SQLException 
	//@Override
	//public void doAction(ContextoAcao contexto) throws Exception
	{

		//Conecta no banco do Sankhya
		ConnectMSSQLServer.dbConnect("jdbc:sqlserver://192.168.0.5:1433;DatabaseName=SANKHYA_PROD;", "adriano","Compiles23");

		EmailPedidoTransporte.PedidoTransporte();
		System.out.println("Transporte concluido!\n");

		EmailPedidoNFEmitida.NFEmitida();
		System.out.println("NF Emitida Concluido!\n");

		//recupera o numero da negociação
		String comando="select nunota, codparc, codvend, codcontato from tgfcab " + 
				"where statusnota='L' and ad_envioemailsepestoque is null and ad_envioemailpropaprovada='SIM'"+
				" and codtipoper in(204,205,900,901,912,925,931)and dtalter>='2017-09-28'";
		System.out.println(comando);

		Statement smnt = ConnectMSSQLServer.conn.createStatement(); 
		smnt.execute(comando);
		ResultSet result = smnt.executeQuery(comando);

		while(result.next())
		{
			EmailPedidoEmSeparacao.PedidoSeparacao(result.getInt("nunota"), result.getString("codvend"), result.getString("codparc"), result.getString("codcontato"));;
		}

		System.out.println("Pedido Separacao Concluido!\n");

		EmailPedidoRealizado.PedidoRealizado();
		System.out.println("Pedido Realizado Concluido!\n");



	}

}
