/**
 *
 */
package com.publiclibs.networkutils.info2bytes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author freedom1b2830
 * @date 2023-апреля-22 12:40:05
 */
public class Info2bytes {
	public static final byte ipv4 = 0x04;
	public static final byte ipv6 = 0x06;

	public static byte[] fromListToBytes(final List<InetAddress> addresses) throws IOException {
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		byteArrayOutputStream.write(addresses.size());
		for (final InetAddress inetAddress : addresses) {
			// FAM
			final Class<? extends InetAddress> classs = inetAddress.getClass();
			if (classs == Inet4Address.class) {
				byteArrayOutputStream.write(ipv4);
			} else if (classs == Inet6Address.class) {
				byteArrayOutputStream.write(ipv6);
			} else {
				throw new UnsupportedOperationException(classs.getSimpleName());
			}
			final byte[] addr = inetAddress.getAddress();
			byteArrayOutputStream.write(addr);
		}
		return byteArrayOutputStream.toByteArray();
	}

	public static void main(final String[] args) throws IOException {
		final ArrayList<InetAddress> completeList = new ArrayList<>();
		final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		while (interfaces.hasMoreElements()) {
			final NetworkInterface networkInterface = interfaces.nextElement();
			final Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
			while (inetAddresses.hasMoreElements()) {
				final InetAddress addr = inetAddresses.nextElement();
				completeList.add(addr);
			}
		}
		final byte[] bytes = fromListToBytes(completeList);
		System.err.println(Arrays.toString(bytes));
		final List<InetAddress> addresses = toList(bytes);
		int size = 0;

		for (final InetAddress inetAddress : addresses) {
			System.err.println(inetAddress.getClass() + " " + inetAddress);
			size = size + inetAddress.toString().length();
		}
		final String str = new String(bytes, StandardCharsets.UTF_8);
		final int size2 = str.length();
		System.err.println(str);
		System.err.println(size + " " + size2 + " " + bytes.length);

	}

	public static List<InetAddress> toList(final byte[] byteString) throws IOException {
		return toList(new ByteArrayInputStream(byteString));
	}

	public static List<InetAddress> toList(final ByteArrayInputStream byteArrayInputStream) throws IOException {
		// int addresesCount is frame count
		// frame 0:
		// ---FAM of address (4||6)
		// ------bytes for address(see FAM)
		// frame 1:
		// ---FAM of address (4||6)
		// ------bytes for address(see FAM)
		// frame 3:
		// ---FAM of address (4||6)
		// ------bytes for address(see FAM)
		final ArrayList<InetAddress> addresses = new ArrayList<>();
		final int addresesCount = byteArrayInputStream.read();
		for (int i = 0; i < addresesCount; i++) {
			final int fam = byteArrayInputStream.read();
			if (fam != ipv4 && fam != ipv6) {
				throw new UnsupportedOperationException("FAM:" + fam);
			}
			final int addressSize;
			if (fam == ipv4) {
				addressSize = 4;
			} else {
				addressSize = 16;
			}
			final byte[] addressBytes = new byte[addressSize];
			final int readed = byteArrayInputStream.read(addressBytes);
			if (readed != 4 && readed != 16) {
				final Integer readedI = Integer.valueOf(readed);
				final Integer addressesI = Integer.valueOf(addresses.size());
				final String format = "readed: %s addresses:%s";
				final String msg = String.format(format, readedI, addressesI);
				throw new IllegalArgumentException(msg);
			}
			addresses.add(InetAddress.getByAddress(addressBytes));
		}
		if (addresses.isEmpty()) {
			throw new NoSuchElementException("addresses.isEmpty()");
		}
		// see addresesCount
		if (byteArrayInputStream.available() > 0) {
			throw new IllegalStateException("byteArrayInputStream.available()>0");
		}
		// need:
		// !addresses.isEmpty
		// byteArrayInputStream.available() == 0
		return addresses;
	}

}
