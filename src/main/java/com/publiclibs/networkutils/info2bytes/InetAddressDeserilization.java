/**
 *
 */
package com.publiclibs.networkutils.info2bytes;

import static com.publiclibs.networkutils.info2bytes.InetAddressHeader.ipv4;
import static com.publiclibs.networkutils.info2bytes.InetAddressHeader.ipv6;
import static com.publiclibs.networkutils.info2bytes.InetAddressSerilization.serializeListOfInetAddressToBytes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author freedom1b2830
 * @date 2023-апреля-22 13:51:15
 */
public final class InetAddressDeserilization {

	public static List<InetAddress> deserializeByteArrayInputStreamToListOfInetAddress(final ByteArrayInputStream input)
			throws IOException {
		final ArrayList<InetAddress> result = new ArrayList<>();

		final int addresesCount = input.read();
		for (int i = 0; i < addresesCount; i++) {
			result.add(deserializeByteArrayInputStreamToSingleInetAddress(input));
		}
		if (result.isEmpty()) {
			throw new NoSuchElementException("addresses.isEmpty()");
		}
		// see addresesCount
		if (input.available() > 0) {
			throw new IllegalStateException("byteArrayInputStream.available()>0");
		}
		// need:
		// !addresses.isEmpty
		// byteArrayInputStream.available() == 0
		return result;
	}

	public static InetAddress deserializeByteArrayInputStreamToSingleInetAddress(final ByteArrayInputStream input)
			throws IOException {
		final int fam = input.read();
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
		final int readed = input.read(addressBytes);
		if (readed != 4 && readed != 16) {
			final Integer readedI = Integer.valueOf(readed);
			final String format = "readed: %s";
			final String msg = String.format(format, readedI);
			throw new IllegalArgumentException(msg);
		}
		return InetAddress.getByAddress(addressBytes);
	}

	public static List<InetAddress> deserializeBytesToListOfInetAddress(final byte[] bytes) throws IOException {
		return deserializeByteArrayInputStreamToListOfInetAddress(new ByteArrayInputStream(bytes));
	}

	public static InetAddress deserializeBytesToSingleInetAddress(final byte[] bytes) throws IOException {
		return deserializeByteArrayInputStreamToSingleInetAddress(new ByteArrayInputStream(bytes));
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
		for (final InetAddress inetAddress : completeList) {
			System.err.println(inetAddress.getClass() + " " + inetAddress);
		}
		// input
		final byte[] bytes = serializeListOfInetAddressToBytes(completeList);
		System.err.println(Arrays.toString(bytes));

		final List<InetAddress> addresses = deserializeBytesToListOfInetAddress(bytes);
		for (final InetAddress inetAddress : addresses) {
			System.err.println(inetAddress.getClass() + " " + inetAddress);
		}
	}

	private InetAddressDeserilization() {
	}
}
