package com.holo.db;

import java.io.IOException;
import java.lang.System.Logger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import com.holo.util.Talker;
import com.holo.network.ClientHandler;
import com.holo.util.FatalErrors;
import com.holo.util.LoggerLevels;

/**
 * Where all the SQL statements are stored, and how to handle each case from the client
 * @since 0.1.0
 * @version 0.3.0
 */
public class Statements {
    private static final Logger logger = System.getLogger(Statements.class.getName());

    private static int personId(DBConnection con, String name) {
        try {
            PreparedStatement ps = con.getConnection().get().prepareStatement("SELECT id FROM people WHERE name=?");
            ps.setString(1, name);
            ResultSet rs = con.returnResult(ps).orElseThrow();
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {}
        return -1;
    }

    /**
     * Allow the server to craft the required response to the client
     * @param what What the request is
     * @return A string to send back
     */
    public static String craftResponse(String what, DBConnection con, Talker talker, ClientHandler ch) {
        String[] array = what.split(" ");
        switch(array[0]) {
            case "LOGIN": {
                try {
                    PreparedStatement ps1 = con.getConnection().get().prepareStatement("SELECT * FROM ipban WHERE ip_address = ?");
                    ps1.setString(1, ch.getIp());
                    ResultSet rs1 = con.returnResult(ps1).orElseThrow();
                    if (rs1.next()) {
                        logger.log(LoggerLevels.WARNING, ch.getIp() + " has tried to log in, when their IP is banned.");
                        throw new SQLException("Banned IP");
                    }
                    PreparedStatement ps = con.getConnection().get().prepareStatement("SELECT password, banned FROM users WHERE username = ?");
                    ps.setString(1, array[1]);
                    ResultSet rs = con.returnResult(ps).orElseThrow();
                    rs.next();
                    if (rs.getBoolean(2)) {
                        logger.log(LoggerLevels.WARNING, ch.getIp() + " has tried to log into a banned account.");
                        throw new SQLException("Banned user");
                    }
                    return "LOGIN-PASS " + rs.getString(1);
                } catch (SQLException e) {
                    return "LOGIN-PASS NULL";
                }
            }
            case "LOG": {
                logger.log(LoggerLevels.INFO, array[2] + " has logged in.");
                return "LOG-YES";
            }
            case "REGISTER": {
                try {
                    String name = "";
                    PreparedStatement prePS = con.getConnection().get().prepareStatement("INSERT INTO `people`(name) VALUES (?)");
                    PreparedStatement ps = con.getConnection().get().prepareStatement("INSERT INTO `users` VALUES (?, ?, ?, ?, ?)");
                    ps.setString(1, array[1]); // Username
                    ps.setString(2, array[2]); // Password

                    if (array.length > 4) // Name contains a space
                        name = array[3] + "_" + array[4];
                    else // Name does not contain a space
                        name = array[3];

                    prePS.setString(1, name);
                    con.runQuery(prePS); // Register them in the People table

                    ps.setInt(3, personId(con, name));
                    ps.setBoolean(4, false); // isAdmin
                    ps.setBoolean(5, false); // isBanned
                    con.runQuery(ps);
                    return "REGISTRATION-PASS"; // Succeeded
                } catch(SQLException e) {
                    e.printStackTrace();
                    logger.log(LoggerLevels.ERROR, FatalErrors.DATABASE_UNSYNC);
                    return "REGISTRATION-FAIL"; // DB error
                }
            }
            case "REGISTER-ACCOUNT": {
                try {
                    String name;
                    if (array.length > 4) // Name contains a space
                        name = array[3] + "_" + array[4];
                    else // Name does not contain a space
                        name = array[3];
                    PreparedStatement ps1 = con.getConnection().get().prepareStatement("SELECT `id` FROM people WHERE name=?");
                    ps1.setString(1, name);
                    ResultSet rs = con.returnResult(ps1).orElseThrow();
                    rs.next();
                    int id = rs.getInt(1);
                    if (id == 0) {
                        // Something went wrong - kill the client as a precaution
                        return "KILL";
                    }
                    PreparedStatement ps = con.getConnection().get().prepareStatement("INSERT INTO `users` VALUES (?, ?, ?, ?, ?)");
                    ps.setString(1, array[1]); // Username
                    ps.setString(2, array[2]); // Password
                    ps.setInt(3, id);
                    ps.setBoolean(4, false); // isAdmin
                    ps.setBoolean(5, false); // isBanned
                    con.runQuery(ps);
                    return "REGISTRATION-PASS"; // Succeeded
                } catch (SQLException e) {
                    e.printStackTrace();
                    logger.log(LoggerLevels.ERROR, FatalErrors.DATABASE_UNSYNC);
                    return "REGISTRATION-FAIL"; // DB error
                }
            }
            case "DEBT-REGISTER": {
                try{
                    PreparedStatement ps = con.getConnection().get().prepareStatement("INSERT INTO `debt`(`debtor_id`, `debtee_id`, `amount`, `memo`) VALUES (?, ?, ?, ?)");
                    ps.setInt(1, personId(con, array[1]));
                    ps.setInt(2, personId(con, array[2]));
                    ps.setDouble(3, Double.parseDouble(array[3]));
                    ps.setString(4, array[4]);
                    con.runQuery(ps);
                    return "D-REGISTRATION-PASS";
                } catch (SQLException e) {
                    return "D-REGISTRATION-FAIL";
                }
            }
            case "DEBT-UPDATE": {
                try {
                    PreparedStatement ps = con.getConnection().get().prepareStatement("UPDATE `debt` SET `paid`=? WHERE `debt_id`=?");
                    ps.setDouble(1, Double.parseDouble(array[1]));
                    ps.setInt(2, Integer.parseInt(array[2]));
                    con.runQuery(ps);
                    return "D-UPDATE-SUCCESS";
                } catch (SQLException e) {
                    return "D-UPDATE-FAIL";
                }
            }
            case "GET-DEVICES": {
                try {
                    PreparedStatement ps1 = con.getConnection().get().prepareStatement("SELECT COUNT(*) FROM Devices WHERE owner = ?");
                    ps1.setString(1, array[1]);
                    ResultSet rs1 = con.returnResult(ps1).orElseThrow();
                    if (rs1.next())
                        talker.send("DEVICE-TOTAL " + rs1.getInt(1));
                    else 
                        talker.send("DEVICE-TOTAL 0");
                    PreparedStatement ps = con.getConnection().get().prepareStatement("SELECT * FROM Devices WHERE owner = ?");
                    ps.setString(1, array[1]);
                    ResultSet rs = con.returnResult(ps).get();
                    while (rs.next()) {
                        talker.send("DEVICE-SERIAL " + rs.getString(1));
                        talker.send("DEVICE-MAC " + rs.getString(2));
                        talker.send("DEVICE-NAME " + rs.getString(3));
                        talker.send("DEVICE-OWNER " + rs.getString(4));
                    }
                    return "DEVICE-FINISH";
                } catch (IOException | SQLException e) {
                    e.printStackTrace();
                    logger.log(LoggerLevels.WARNING, "Unable to get device information - killing client as a safeguard");
                    return "KILL";
                }
            }
			case "GET-PEOPLE": {
				try {
					PreparedStatement ps1 = con.getConnection().get().prepareStatement("SELECT COUNT(*) FROM people");
					ResultSet rs1 = con.returnResult(ps1).orElseThrow();
					if (rs1.next())
						talker.send("USER-TOTAL " + rs1.getInt(1));
					else
						talker.send("USER-TOTAL 0");
					PreparedStatement ps = con.getConnection().get().prepareStatement("SELECT people.id, users.username, people.name FROM people LEFT JOIN users ON users.name_id = people.id;");
					ResultSet rs = con.returnResult(ps).orElseThrow();
					while (rs.next()) {
						talker.send("USER-ID " + rs.getInt(1));
						talker.send("USER-USERNAME " + rs.getString(2));
						String name = rs.getString(3);
						if (name.contains(" "))
							name.replace(' ', '_');
						talker.send("USER-NAME " + name);
					}
					return "USER-FINISH";
				} catch (IOException | SQLException e) {
					e.printStackTrace();
					logger.log(LoggerLevels.WARNING, "Unable to get user info - killing client as a safeguard.");
					return "KILL";
				}
			}
			case "GET-PERSON": {
				try {
					PreparedStatement ps = con.getConnection().get().prepareStatement("SELECT id FROM people WHERE name = ?");
					ps.setString(1, array[1]);
					ResultSet rs = con.returnResult(ps).orElseThrow();
					if (rs.next())
						return "PERSON-ID " + rs.getString(1);
					else
						return "PERSON-ID -1";
				} catch (SQLException e) {
					
				}
			}
			case "ADD-PERSON": {
				try {
					PreparedStatement ps = con.getConnection().get().prepareStatement("INSERT INTO people(`name`) VALUES (?)");
					ps.setString(1, array[1]);
					con.runQuery(ps);
                    return "ADD-PERSON-OK";
				} catch (SQLException e) {
					e.printStackTrace();
					logger.log(LoggerLevels.WARNING, "Unable to add a person to the database.");
                    return "ADD-PERSON-NO";
				}
			}
            case "GET-DEVICES-ALL": {
                try {
                    PreparedStatement ps1 = con.getConnection().get().prepareStatement("SELECT COUNT(*) FROM Devices");
                    ResultSet rs1 = con.returnResult(ps1).get();
                    if (rs1.next())
                        talker.send("DEVICE-TOTAL " + rs1.getInt(1));
                    else
                        talker.send("DEVICE TOTAL 0");
                    PreparedStatement ps = con.getConnection().get().prepareStatement("SELECT * FROM Devices");
                    ResultSet rs = con.returnResult(ps).orElseThrow();
                    while (rs.next()) {
                        talker.send("DEVICE-SERIAL " + rs.getString(1));
                        talker.send("DEVICE-MAC " + rs.getString(2));
                        talker.send("DEVICE-NAME " + rs.getString(3));
                        talker.send("DEVICE-OWNER " + rs.getString(4));
                    }
                    return "DEVICE-FINISH";
                } catch (IOException | SQLException e) {
                    e.printStackTrace();
                    logger.log(LoggerLevels.WARNING, "Unable to get device information - killing client as a safeguard");
                    return "KILL";
                }
            }
            case "ADMIN-RESPONSE": {
                try {
                    PreparedStatement ps = con.getConnection().get().prepareStatement("SELECT is_admin FROM Users WHERE username = ?");
                    ps.setString(1, array[1]);
                    ResultSet rs = con.returnResult(ps).orElseThrow();
                    if (rs.next()) {
                        if (rs.getBoolean(1)) {
                            ch.setAdmin(true);
                            return "YES";
                        }
                    }
                    ch.setAdmin(false);
                    return "NO";
                } catch (SQLException e) {
                    e.printStackTrace();
                    logger.log(LoggerLevels.WARNING, "Admin table may not exist");
                    ch.setAdmin(false);
                    return "NO";
                }
            }
            case "REGISTER-DEVICE": {
                String owner = array[1];
                String deviceSerial = array[2];
                String macAddress = array[3];
                String deviceName = array[4];
                try {
                    PreparedStatement ps = con.getConnection().get().prepareStatement("INSERT INTO `devices` VALUES (?, ?, ?, ?, ?)");
                    ps.setString(1, deviceSerial);
                    ps.setString(2, macAddress);
                    ps.setString(3, deviceName);
                    ps.setString(4, owner);
                    ps.setBoolean(5, false);
                    con.runQuery(ps);
                    return "DEVICE-YES";
                } catch (SQLException e) {
                    e.printStackTrace();
                    logger.log(LoggerLevels.WARNING, "Recieved incorrect response - Device is not registred.");
                    return "DEVICE-NO";
                }
            }
            case "UPDATE-DEVICE": {
                String owner = array[1];
                String deviceSerial = array[2];
                String macAddress = array[3];
                String deviceName = array[4];
                if (owner.equals(ch.getUsername().orElse("")) || ch.getAdmin())
                    try {
                        PreparedStatement ps = con.getConnection().get().prepareStatement("UPDATE devices SET mac_address=?, device_name=?, owner=? WHERE serial_number=?");
                        ps.setString(1, macAddress);
                        ps.setString(2, deviceName);
                        ps.setString(3, owner);
                        ps.setString(4, deviceSerial);
                        con.runQuery(ps);
                        return "DEVICE-UPDATE-YES";
                    } catch (SQLException e) {
                        e.printStackTrace();
                        logger.log(LoggerLevels.WARNING, "No device with that serial number exists");
                        return "DEVICE-UPDATE-NO";
                    }
                else {
                    logger.log(LoggerLevels.WARNING, ch.getUsername() + " tried to modify a device they had no permission to edit.");
                    return "DEVICE-UPDATE-NO";
                }
            }
            case "DELETE-DEVICE": {
                String owner = array[1];
                String deviceSerial = array[2];
                String macAddress = array[3];
                String deviceName = array[4];
                if (owner.equals(ch.getUsername().orElse("")) || ch.getAdmin())
                    try {
                        PreparedStatement ps = con.getConnection().get().prepareStatement("DELETE FROM devices WHERE mac_address=? AND device_name=? AND owner=? AND serial_number=?");
                        ps.setString(1, macAddress);
                        ps.setString(2, deviceName);
                        ps.setString(3, owner);
                        ps.setString(4, deviceSerial);
                        con.runQuery(ps);
                        return "DEVICE-DELETE-YES";
                    } catch (SQLException e) {
                        e.printStackTrace();
                        logger.log(LoggerLevels.WARNING, "No device with that serial number exists");
                        return "DEVICE-DELETE-NO";
                    }
                else {
                    logger.log(LoggerLevels.WARNING, ch.getUsername() + " tried to delete a device they had no permission to delete.");
                    return "DEVICE-DELETE-NO";
                }
            }
			case "GET-AUTHOR-ID": {
				try {
					PreparedStatement ps = con.getConnection().get().prepareStatement("SELECT * FROM authors WHERE name=?");
					ps.setString(1, array[1]);
					ResultSet rs = con.returnResult(ps).orElseThrow();
					if (rs.next()) {
						return String.valueOf(rs.getInt(1));
					} else {
						return "-1";
					}
				} catch (SQLException e) {
					return "-1";
				}
			}
			case "ADD-AUTHOR": {
				try {
					PreparedStatement ps = con.getConnection().get().prepareStatement("INSERT INTO authors(name) VALUES (?)");
					ps.setString(1, array[1]);
					con.runQuery(ps);
					return "ADD-AUTHOR-SUCCESS";
				} catch (SQLException e) {
					return "ADD-AUTHOR-FAILURE";
				}
			}
			case "REGISTER-BOOK": {
				try {
					PreparedStatement ps = con.getConnection().get().prepareStatement("INSERT INTO books VALUES (?, ?, ?, ?)");
					ps.setString(1, array[1]);
					ps.setInt(2, Integer.parseInt(array[3]));
					ps.setString(3, array[4]);
					ps.setString(4, array[2]);
					con.runQuery(ps);
					return "ADD-BOOK-SUCCESS";
				} catch (SQLException e) {
					return "ADD-BOOK-FAILURE";
				}
			}
			case "GET-BOOKS": {
				try {
					PreparedStatement ps1 = con.getConnection().get().prepareStatement("SELECT COUNT(*) FROM Books");
					ResultSet rs1 = con.returnResult(ps1).orElseThrow();
					if (rs1.next()) {
						talker.send("BOOK-TOTAL " + rs1.getInt(1));
					} else {
						talker.send("BOOK-TOTAL 0");
					}
					PreparedStatement ps2 = con.getConnection().get().prepareStatement("SELECT authors.name AS 'Author Name', books.name AS 'Book Name', books.isbn, books.book_id FROM authors, books WHERE books.author_id = authors.author_id;");
					ResultSet rs2 = con.returnResult(ps2).orElseThrow();
					while (rs2.next()) {
						talker.send("BOOK-TITLE " + rs2.getString(2));
						talker.send("BOOK-AUTHOR " + rs2.getString(1));
						talker.send("BOOK-ISBN " + rs2.getString(3));
						talker.send("BOOK-ID " + rs2.getString(4));
						PreparedStatement ps3 = con.getConnection().get().prepareStatement("SELECT people.id, people.name, bookrentals.due_date FROM people, bookrentals, books WHERE books.book_id = bookrentals.book_id AND bookrentals.rentor = people.id AND books.book_id = ? AND bookrentals.returned = 0;");
						ps3.setString(1, rs2.getString(4));
						ResultSet rs3 = con.returnResult(ps3).orElseThrow();
						if (rs3.next()) {
							talker.send("BOOK-MORE");
							talker.send("USER-NAME " + rs3.getString(2));
							talker.send("USER-ID " + rs3.getString(1));
							talker.send("BOOK-DUE " + rs3.getTimestamp(3).toLocalDateTime().toString());
						} else {
							talker.send("BOOK-NEXT");
						}
					}
					return "BOOK-FINISH";
				} catch (IOException | SQLException e) {
					return "BOOK-FINISH";
				}
			}
			case "RENT-BOOK": {
                LocalDate ldt = LocalDate.now();
                LocalDate dueTime = ldt.plusWeeks(2);
				try {
					PreparedStatement ps1 = con.getConnection().get().prepareStatement("INSERT INTO bookrentals(book_id, rentor, rented_date, due_date) VALUES (?, ?, ?, ?)");
					ps1.setString(1, array[1]);
					ps1.setInt(2, Integer.parseInt(array[2]));
					ps1.setString(3, ldt.toString());
					ps1.setString(4, dueTime.toString());
					con.runQuery(ps1);
				} catch (SQLException e) {
					return "NULL";
				}
				return "RENT-BOOK-SUCCESS";
			}
			case "UNRENT-BOOK": {
				LocalDate ld = LocalDate.now();
				try {
					PreparedStatement ps1 = con.getConnection().get().prepareStatement("UPDATE bookrentals SET returned=1, return_date=? WHERE book_id=? AND returned=0");
                    ps1.setString(1, ld.toString());
					ps1.setString(2, array[1]);
					con.runQuery(ps1);
					return "UNRENT-BOOK-SUCCESS";
				} catch (SQLException e) {
					return "UNRENT-BOOK-FAILURE";
				}
			}
        }
        return "KILL"; // Default stance - kill client
    }
}
