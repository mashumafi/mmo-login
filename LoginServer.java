import java.net.ServerSocket;
import java.net.Socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.DriverManager;

public class LoginServer
{
    public static void main(String[] args) throws IOException
    {
        ServerSocket serverSocket = null;
        boolean listening = true;

        try
        {
            serverSocket = new ServerSocket(8080);
        }
        catch (Exception e)
        {
			if(Global.debug)
			e.printStackTrace();
            //System.exit(-1);
        }

        while (listening)
        {
			new LoginMultiServerThread(serverSocket.accept());
		}

        serverSocket.close();
    }
}

class LoginMultiServerThread extends Thread
{
	Socket socket = null;
	String Nickname;
	String Password;
	Connection con;
	BufferedReader inStream;

	public LoginMultiServerThread(Socket socket)
	{
		super("ServerThread");
		this.socket = socket;

		try
		{
			inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection("jdbc:mysql://localhost:3306/mashumafi", "root", "m1a9t9t1");
			//new Thread(this).start();
			this.start();
		}
		catch(Exception e)
		{
			if(Global.debug)
			e.printStackTrace();
		}
	}

    public void run()
    {
			String inputLine;

		try
		{
			while((inputLine = inStream.readLine()) != null)
			{
				try
				{
					ProcessInput(inputLine);
				}
				catch(Exception e)
				{
					if(Global.debug)
					e.printStackTrace();
				}
			}
		}
		catch (Exception e)
		{
			if(Global.debug)
			e.printStackTrace();
		}
		try
		{
			socket.close();
		}
		catch (Exception e)
		{
			if(Global.debug)
			e.printStackTrace();
		}
    }

    public void send(final String msg)
    {
		try
		{
			new PrintStream(socket.getOutputStream()).println(msg);
		}
		catch (Exception e)
		{
			if(Global.debug)
			e.printStackTrace();
		}
	}

	public void ProcessInput(final String input)
	{
		String theOutput = input;
		System.out.println(input);
		String[] data;
		data = between(input,":");
		if(data.length == 0)
		{
			return;
		}
		if(data[0].equals("Login"))
		{
			LoginDatabase(data[1],data[2]);
		}
		if(data[0].equals("Play"))
		{
			Play(data);
		}
		if(data[0].equals("CreateCharacter"))
		{
			System.out.println(input);
			CreateCharacter(data);
		}
		if(data[0].equals("DeleteCharacter"))
		{
			DeleteCharacter(data);
		}
		if(data[0].equals("CreateAccount"))
		{
			CreateAccount(data);
		}
	}

	public String[] between(String string, String search)
	{
		String[] a = new String[0];
		int count = 0;
		for(int i = 0; ; i++)
		{
			if(string.lastIndexOf(search) < count) break;
			a = add(a, string.substring(count, string.indexOf(search,count)));
			count = string.indexOf(search, count)+1;
		}
		return a;
	}

	private String[] add(String[] a, String add)
	{
		if(add.length() > 0)
			{
			String[] temp = a;
			a = new String[a.length+1];
			for(int i = 0; i < temp.length; i++)
			{
				a[i] = temp[i];
			}
			a[a.length-1] = add;
		}
		return a;
	}

	public void CreateAccount(String data[])
	{
		Statement stmt;
		ResultSet rs;
		ResultSetMetaData rsmd;
		String Email, Name, Password;
		Email = data[1];
		Name = data[2];
		Password = data[3];

		try
		{
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

			rs = stmt.executeQuery("select * from Accounts where Nickname='" + Name + "' or Email='" + Email + "'");
			rsmd = rs.getMetaData();

			if(!rs.next())
			{
				rs.moveToInsertRow();
				rs.updateString(2, Email);
				rs.updateString(3, Name);
				rs.updateString(4, Password);
				rs.insertRow();
				send("AccountCreationSuccess:");
			}
			else
			{
				send("AccountCreationFailure:");
			}
		}
		catch(Exception e)
		{
			if(Global.debug)
			e.printStackTrace();
		}
	}

	public void CreateCharacter(String data[])
	{
		Statement stmt;
		ResultSet rs;
		ResultSetMetaData rsmd;
		String Server = "", Name = data[2];
		int IntServer = Integer.parseInt(data[1]);
		int gender = Integer.parseInt(data[3])+1;
		boolean log = true;

		switch(IntServer)
		{
			case 0:
			{
				Server = "mashumafi";
			}
			default:
			{
				log = false;
				break;
			}
		}
		try
		{
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

			rs = stmt.executeQuery("select * from Characters where Name='" + Name + "' and Server='" + Server + "'");
			rsmd = rs.getMetaData();
			if(!rs.next())
			{
				rs = stmt.executeQuery("select * from Accounts where Nickname='" + Nickname + "'");
				rsmd = rs.getMetaData();
				rs.next();
				int account = rs.getInt("Accounts.ID");
				rs = stmt.executeQuery("select * from Characters");
				rsmd = rs.getMetaData();
				rs.moveToInsertRow();
				rs.updateInt("Characters.Account", account);
				rs.updateString("Characters.Server", Server);
				rs.updateString("Characters.Name", Name);
				rs.updateInt("Characters.Frame", 0);
				rs.updateBoolean("Characters.Flip", false);
				rs.updateInt("Characters.Gender", gender);
				rs.updateString("Characters.SkinColor", "Basic");
				rs.updateString("Characters.HairStyle", "Basic");
				rs.updateString("Characters.HairColor", "Basic");
				rs.insertRow();
				rs.last();
				int character = rs.getInt("Characters.ID");
				rs = stmt.executeQuery("select * from Items");
				rsmd = rs.getMetaData();
				rs.moveToInsertRow(); rs.updateInt("Items.Account", character); rs.updateString("Items.Image", "Basic"); rs.updateInt("Items.Slot", 9); rs.updateString("Items.Type", "Top"); rs.updateInt("Items.Gender", gender); rs.updateInt("Items.PossibleSlot", 9); rs.updateInt("Items.Channel", 1); rs.insertRow();
				rs.moveToInsertRow(); rs.updateInt("Items.Account", character); rs.updateString("Items.Image", "Basic"); rs.updateInt("Items.Slot", 17); rs.updateString("Items.Type", "Bottom"); rs.updateInt("Items.Gender", gender); rs.updateInt("Items.PossibleSlot", 17); rs.updateInt("Items.Channel", 1); rs.insertRow();
				rs.moveToInsertRow(); rs.updateInt("Items.Account", character); rs.updateString("Items.Image", "Basic"); rs.updateInt("Items.Slot", 33); rs.updateString("Items.Type", "Shoes"); rs.updateInt("Items.Gender", gender); rs.updateInt("Items.PossibleSlot", 33); rs.updateInt("Items.Channel", 1); rs.insertRow();
				rs.moveToInsertRow(); rs.updateInt("Items.Account", character); rs.updateString("Items.Image", "Basic"); rs.updateInt("Items.Slot", 8); rs.updateString("Items.Type", "Primary"); rs.updateInt("Items.Gender", gender); rs.updateInt("Items.PossibleSlot", 8); rs.updateInt("Items.Channel", 1); rs.insertRow();
				LoginDatabase(Nickname,Password);
			}
		}
		catch(Exception e)
		{
			if(Global.debug)
			e.printStackTrace();
		}
	}

	public void DeleteCharacter(String data[])
	{
		Statement stmt;
		ResultSet rs;
		ResultSetMetaData rsmd;
		String Server = "";
		String CharName = data[2];
		String tempPass = data[3];
		int IntServer = Integer.parseInt(data[1]);
		boolean log = true;

		switch(IntServer)
		{
			case 0:
			{
				Server = "mashumafi";
			}
			default:
			{
				log = false;
				break;
			}
		}
		try
		{
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

			rs = stmt.executeQuery("select * from Accounts where Nickname='" + Nickname + "' and Password='" + tempPass + "'");
			rsmd = rs.getMetaData();

			if(rs.next())
			{
				rs = stmt.executeQuery("select * from Characters inner join Accounts on Characters.Account=Accounts.ID where Characters.Name='" + CharName + "' and Characters.Server='" + Server + "' and Accounts.Nickname='"+ Nickname + "'");
				rsmd = rs.getMetaData();
				rs.next();
				int chara = rs.getInt("Characters.ID");
				rs = stmt.executeQuery("select * from Items where Account='" + chara + "'");
				while(rs.next())
					rs.deleteRow();
				rs = stmt.executeQuery("select * from Characters where Name='" + CharName + "' and Server='" + Server + "'");
				rsmd = rs.getMetaData();
				rs.next();
				rs.deleteRow();
				LoginDatabase(Nickname,tempPass);
			}
		}
		catch(Exception e)
		{
			if(Global.debug)
			e.printStackTrace();
		}
	}

	public void Play(String data[])
	{
		Statement stmt;
		ResultSet rs;
		ResultSetMetaData rsmd;
		int IntServer = Integer.parseInt(data[2]), IntChannel = Integer.parseInt(data[3]);
		String Chara = data[1], Server = "", Network = "";
		int Channel = 0;
		boolean log = true;

		switch(IntServer)
		{
			case 0:
			{
				switch(IntChannel)
				{
					case 0:
					{
						Server = "mashumafi";
						Network = ":71.58.9.96:8000:";
						break;
					}
					default:
					{
						log = false;
						break;
					}
				}
			}
			default:
			{
				log = false;
				break;
			}
		}

		try
		{
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

			rs = stmt.executeQuery("select * from Accounts inner join Characters on Characters.Account=Accounts.ID where Accounts.Nickname='" + Nickname + "' and Characters.Name='" + Chara + "' and Characters.Server='" + Server + "'");
			rsmd = rs.getMetaData();
			if(rs.next())
			{
				System.out.println("Play:" + rs.getString("Characters.Name") + ":" + Network);
				send("Play:" + rs.getString("Characters.Name") + Network + rs.getString("Characters.X") + ":" + rs.getString("Characters.Y") + ":" + rs.getString("Characters.Map") + ":");
				//System.out.println("Play:" + rs.getString("characters.name") + Network);
			}
		}
		catch(Exception e)
		{
			if(Global.debug)
			e.printStackTrace();
		}
	}

	public void LoginDatabase(String nickname, String password)
	{
		Statement stmt;
		ResultSet rs;
		ResultSetMetaData rsmd;
		Statement stmt2;
		ResultSet rs2;
		ResultSetMetaData rsmd2;
		String data = "Login:";
		Nickname = nickname;
		Password = password;
		String datas[];

		try
		{
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

			rs = stmt.executeQuery("select Nickname,Password from Accounts where Nickname='" + Nickname + "'");
			rsmd = rs.getMetaData();

			if(rs.next())
			if(rs.getString("Accounts.Password").equals(Password))
			{
				rs = stmt.executeQuery("select Characters.ID, Characters.Name, Characters.Server, Characters.Gender, Characters.SkinColor, Characters.HairStyle, Characters.HairColor from Accounts inner join Characters on Accounts.ID=Characters.Account where Characters.Server='mashumafi' and Accounts.Nickname='" + Nickname + "'");
				rsmd = rs.getMetaData();

				while(rs.next())
				{
					for(int j = 2; j <= rsmd.getColumnCount(); j++)
					{
						data += rsmd.getColumnLabel(j) + ":" + rs.getString(j) + ":";
					}
					stmt2 = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
					rs2 = stmt2.executeQuery("select items.type, items.image from items where items.account="+rs.getString("Characters.ID")+" and items.slot<100");
					rsmd2 = rs2.getMetaData();
					while(rs2.next())
					{
						for(int j = 1; j <= rsmd2.getColumnCount(); j++)
						{
							data += rs2.getString(j) + ":";
						}
					}
				}
				send(data);
				System.out.println(data);
			}
		}
		catch(Exception e)
		{
			if(Global.debug)
			e.printStackTrace();
		}
	}
}

class Global
{
	static boolean debug = true;
	static String server = "mashumafi";
	static String channel = "mashumafi1";
}