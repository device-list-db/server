package com.holo.db;

import com.holo.util.Talker;
import com.holo.db.StatementClasses.AddAuthor;
import com.holo.db.StatementClasses.AddPerson;
import com.holo.db.StatementClasses.AdminResponse;
import com.holo.db.StatementClasses.DebtRegister;
import com.holo.db.StatementClasses.DebtUpdate;
import com.holo.db.StatementClasses.DeleteDevice;
import com.holo.db.StatementClasses.GetAuthorId;
import com.holo.db.StatementClasses.GetBooks;
import com.holo.db.StatementClasses.GetDevices;
import com.holo.db.StatementClasses.GetDevicesAll;
import com.holo.db.StatementClasses.GetPeople;
import com.holo.db.StatementClasses.GetPerson;
import com.holo.db.StatementClasses.Login;
import com.holo.db.StatementClasses.Register;
import com.holo.db.StatementClasses.RegisterAccount;
import com.holo.db.StatementClasses.RegisterBook;
import com.holo.db.StatementClasses.RegisterDevice;
import com.holo.db.StatementClasses.RentBook;
import com.holo.db.StatementClasses.UnrentBook;
import com.holo.db.StatementClasses.UpdateDevice;
import com.holo.network.ClientHandler;

/**
 * Where all the SQL statements are stored, and how to handle each case from the client
 * @since 0.1.0
 * @version 0.4.0
 */
public class Statements {

    /**
     * Allow the server to craft the required response to the client
     * @param what What the request is
     * @return A string to send back
     */
    public static String craftResponse(String what, DBConnection con, Talker talker, ClientHandler ch) {
        String[] array = what.split(" ");
        switch(array[0]) {
            case "LOGIN":
                return new Login(con, ch, array).run();
            case "REGISTER":
                return new Register(con, array).run();
            case "REGISTER-ACCOUNT":
                return new RegisterAccount(con, array).run();
            case "DEBT-REGISTER":
                return new DebtRegister(con, array).run();
            case "DEBT-UPDATE":
                return new DebtUpdate(con, array).run();
            case "GET-DEVICES":
                return new GetDevices(con, talker, array).run();
			case "GET-PEOPLE": 
				return new GetPeople(con, talker).run();
			case "GET-PERSON":
				return new GetPerson(con, array).run();
			case "ADD-PERSON":
				return new AddPerson(con, array).run();
            case "GET-DEVICES-ALL":
                return new GetDevicesAll(con, talker).run();
            case "ADMIN-RESPONSE":
                return new AdminResponse(con, ch, array).run();
            case "REGISTER-DEVICE":
                return new RegisterDevice(con, array).run();
            case "UPDATE-DEVICE":
                return new UpdateDevice(con, array, ch).run();
            case "DELETE-DEVICE":
                return new DeleteDevice(con, array, ch).run();
			case "GET-AUTHOR-ID":
				return new GetAuthorId(con, array).run();
			case "ADD-AUTHOR":
				return new AddAuthor(con, array).run();
			case "REGISTER-BOOK":
				return new RegisterBook(con, array).run();
			case "GET-BOOKS":
				return new GetBooks(con, talker).run();
			case "RENT-BOOK":
                return new RentBook(con, array).run();
			case "UNRENT-BOOK":
				return new UnrentBook(con, array).run();
        }
        return "KILL"; // Default stance - kill client
    }
}
