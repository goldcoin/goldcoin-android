/*
 * Copyright 2011-2013 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.schildbach.wallet.goldcoin;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Date;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.DateUtils;

import com.google.goldcoin.core.Utils;

import de.schildbach.wallet.goldcoin.util.IOUtils;

/**
 * @author Andreas Schildbach
 */
public class ExchangeRatesProvider extends ContentProvider
{
	public static class ExchangeRate
	{
		public ExchangeRate(final String currencyCode, final BigInteger rate, final String source)
		{
			this.currencyCode = currencyCode;
			this.rate = rate;
			this.source = source;
		}

		public final String currencyCode;
		public final BigInteger rate;
		public final String source;
	}

	public static final String KEY_CURRENCY_CODE = "currency_code";
	private static final String KEY_RATE = "rate";
	private static final String KEY_SOURCE = "source";

	private Map<String, ExchangeRate> exchangeRates = null;
	private long lastUpdated = 0;

	private static final long UPDATE_FREQ_MS = DateUtils.HOUR_IN_MILLIS;
	private static final int TIMEOUT_MS = 15 * (int) DateUtils.SECOND_IN_MILLIS;

	@Override
	public boolean onCreate()
	{
		return true;
	}

	public static Uri contentUri(final String packageName)
	{
		return Uri.parse("content://" + packageName + '.' + "exchange_rates");
	}

	@Override
	public Cursor query(final Uri uri, final String[] projection, final String selection, final String[] selectionArgs, final String sortOrder)
	{
		final long now = System.currentTimeMillis();

		if (exchangeRates == null || now - lastUpdated > UPDATE_FREQ_MS)
		{
			Map<String, ExchangeRate> newExchangeRates = getBitcoinCharts();//getLitecoinCharts();

			if (exchangeRates == null && newExchangeRates == null)
				newExchangeRates = getBlockchainInfo();

            if(newExchangeRates == null)
                newExchangeRates = getLitecoinCharts();


			if (newExchangeRates != null)
			{
				exchangeRates = newExchangeRates;
				lastUpdated = now;
			}
		}

		if (exchangeRates == null)
			return null;

		final MatrixCursor cursor = new MatrixCursor(new String[] { BaseColumns._ID, KEY_CURRENCY_CODE, KEY_RATE, KEY_SOURCE });

		if (selection == null)
		{
			for (final Map.Entry<String, ExchangeRate> entry : exchangeRates.entrySet())
			{
				final ExchangeRate rate = entry.getValue();
				cursor.newRow().add(entry.getKey().hashCode()).add(rate.currencyCode).add(rate.rate.longValue()).add(rate.source);
			}
		}
		else if (selection.equals(KEY_CURRENCY_CODE))
		{
			final String code = selectionArgs[0];
			final ExchangeRate rate = exchangeRates.get(code);
            try {
			  cursor.newRow().add(code.hashCode()).add(rate.currencyCode).add(rate.rate.longValue()).add(rate.source);
            } catch (NullPointerException e) {
                Log.e("GoldCoin", "Unable to add an exchange rate.  NullPointerException.");
            }
		}

		return cursor;
	}

	public static ExchangeRate getExchangeRate(final Cursor cursor)
	{
		final String currencyCode = cursor.getString(cursor.getColumnIndexOrThrow(ExchangeRatesProvider.KEY_CURRENCY_CODE));
		final BigInteger rate = BigInteger.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow(ExchangeRatesProvider.KEY_RATE)));
		final String source = cursor.getString(cursor.getColumnIndexOrThrow(ExchangeRatesProvider.KEY_SOURCE));

		return new ExchangeRate(currencyCode, rate, source);
	}

	@Override
	public Uri insert(final Uri uri, final ContentValues values)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int update(final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int delete(final Uri uri, final String selection, final String[] selectionArgs)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getType(final Uri uri)
	{
		throw new UnsupportedOperationException();
	}
    private static Double lastLitecoinValue = 0.0;
    private static long lastLitecoinValueCheck = 0;

    private static Double getGoldCoinValueLTC()
    {
        Date date = new Date();
        long now = date.getTime();

        if((now < (lastLitecoinValueCheck + 10 * 60)) && lastLitecoinValue > 0.0)
             return lastLitecoinValue;


        //final Map<String, ExchangeRate> rates = new TreeMap<String, ExchangeRate>();
        // Keep the LTC rate around for a bit
        Double ltcRate = 0.0;
        String currencyCryptsy = "LTC";
        String urlCryptsy = "http://pubapi.cryptsy.com/api.php?method=marketdata";





        try {
            // final String currencyCode = currencies[i];
            final URL URLCryptsy = new URL(urlCryptsy);
            final URLConnection connectionCryptsy = URLCryptsy.openConnection();
            connectionCryptsy.setConnectTimeout(TIMEOUT_MS);
            connectionCryptsy.setReadTimeout(TIMEOUT_MS);
            connectionCryptsy.connect();

            final StringBuilder contentCryptsy = new StringBuilder();

            Reader reader = null;
            try
            {
                reader = new InputStreamReader(new BufferedInputStream(connectionCryptsy.getInputStream(), 1024));
                IOUtils.copy(reader, contentCryptsy);
                final JSONObject head = new JSONObject(contentCryptsy.toString());
                JSONObject returnObject = head.getJSONObject("return");
                JSONObject markets = returnObject.getJSONObject("markets");
                JSONObject GLD = markets.getJSONObject("GLD");

                Double lastTrade = GLD.getDouble("lasttradeprice");

                //String euros = String.format("%.7f", lastTrade);
                // Fix things like 3,1250
                //euros = euros.replace(",", ".");
                //rates.put(currencyCryptsy, new ExchangeRate(currencyCryptsy, Utils.toNanoCoins(euros), URLCryptsy.getHost()));
                if(currencyCryptsy.equalsIgnoreCase("LTC")) ltcRate = lastTrade;

                lastLitecoinValue = ltcRate;
                lastLitecoinValueCheck = now;
            }
            finally
            {
                if (reader != null)
                    reader.close();
            }
            return ltcRate;
        }
        catch (final IOException x)
        {
            x.printStackTrace();
        }
        catch (final JSONException x)
        {
            x.printStackTrace();
        }

        return null;
    }
    private static Double lastBitcoinValue = 0.0;
    private static long lastBitcoinValueCheck = 0;

    private static Object getGoldCoinValueBTC()
    {
        Date date = new Date();
        long now = date.getTime();

        if((now < (lastBitcoinValueCheck + 10 * 60)) && lastBitcoinValue > 0.0)
            return lastBitcoinValue;


        //final Map<String, ExchangeRate> rates = new TreeMap<String, ExchangeRate>();
        // Keep the LTC rate around for a bit
        Double btcRate = 0.0;
        String currencyCryptsy = "BTC";
        String urlCryptsy2 = "http://pubapi.cryptsy.com/api.php?method=marketdata";
        String urlCryptsy = "http://pubapi.cryptsy.com/api.php?method=singlemarketdata&marketid=30";




        try {
            // final String currencyCode = currencies[i];
            final URL URLCryptsy = new URL(urlCryptsy);
            final URLConnection connectionCryptsy = URLCryptsy.openConnection();
            connectionCryptsy.setConnectTimeout(TIMEOUT_MS * 2);
            connectionCryptsy.setReadTimeout(TIMEOUT_MS * 2);
            connectionCryptsy.connect();

            final StringBuilder contentCryptsy = new StringBuilder();

            Reader reader = null;
            try
            {
                reader = new InputStreamReader(new BufferedInputStream(connectionCryptsy.getInputStream(), 1024));
                IOUtils.copy(reader, contentCryptsy);
                final JSONObject head = new JSONObject(contentCryptsy.toString());
                JSONObject returnObject = head.getJSONObject("return");
                JSONObject markets = returnObject.getJSONObject("markets");
                JSONObject GLD = markets.getJSONObject("GLD");



                JSONArray recenttrades = GLD.getJSONArray("recenttrades");

                double btcTraded = 0.0;
                double gldTraded = 0.0;

                for(int i = 0; i < recenttrades.length(); ++i)
                {
                    JSONObject trade = (JSONObject)recenttrades.get(i);

                    btcTraded += trade.getDouble("total");
                    gldTraded += trade.getDouble("quantity");

                }

                Double averageTrade = btcTraded / gldTraded;



                //Double lastTrade = GLD.getDouble("lasttradeprice");



                //String euros = String.format("%.7f", averageTrade);
                // Fix things like 3,1250
                //euros = euros.replace(",", ".");
                //rates.put(currencyCryptsy, new ExchangeRate(currencyCryptsy, Utils.toNanoCoins(euros), URLCryptsy.getHost()));
                if(currencyCryptsy.equalsIgnoreCase("BTC")) btcRate = averageTrade;

                lastBitcoinValue = btcRate;
                lastBitcoinValueCheck = now;
            }
            finally
            {
                if (reader != null)
                    reader.close();
            }
            return btcRate;
        }
        catch (final IOException x)
        {
            x.printStackTrace();
        }
        catch (final JSONException x)
        {
            x.printStackTrace();
        }

        return null;
    }

    private static Object getGoldCoinValueBTC_ccex()
    {
        Date date = new Date();
        long now = date.getTime();

        if((now < (lastBitcoinValueCheck + 10 * 60)) && lastBitcoinValue > 0.0)
            return lastBitcoinValue;


        //final Map<String, ExchangeRate> rates = new TreeMap<String, ExchangeRate>();
        // Keep the LTC rate around for a bit
        Double btcRate = 0.0;
        String currencyCryptsy = "BTC";
        String urlCryptsy = "https://c-cex.com/t/gld-btc.json";

        try {
            // final String currencyCode = currencies[i];
            final URL URLCryptsy = new URL(urlCryptsy);
            final URLConnection connectionCryptsy = URLCryptsy.openConnection();
            connectionCryptsy.setConnectTimeout(TIMEOUT_MS * 2);
            connectionCryptsy.setReadTimeout(TIMEOUT_MS * 2);
            connectionCryptsy.connect();

            final StringBuilder contentCryptsy = new StringBuilder();

            Reader reader = null;
            try
            {
                reader = new InputStreamReader(new BufferedInputStream(connectionCryptsy.getInputStream(), 1024));
                IOUtils.copy(reader, contentCryptsy);
                final JSONObject head = new JSONObject(contentCryptsy.toString());

                //JSONObject returnObject = head.getJSONObject("return");
                //JSONObject markets = returnObject.getJSONObject("markets");
                JSONObject GLD = head.getJSONObject("ticker");



                //JSONArray recenttrades = GLD.getJSONArray("recenttrades");

                double btcTraded = 0.0;
                double gldTraded = 0.0;

                /*for(int i = 0; i < recenttrades.length(); ++i)
                {
                    JSONObject trade = (JSONObject)recenttrades.get(i);

                    btcTraded += trade.getDouble("total");
                    gldTraded += trade.getDouble("quantity");

                }

                Double averageTrade = btcTraded / gldTraded;
                */

                Double averageTrade = head.getDouble("buy");



                //Double lastTrade = GLD.getDouble("lasttradeprice");



                //String euros = String.format("%.7f", averageTrade);
                // Fix things like 3,1250
                //euros = euros.replace(",", ".");
                //rates.put(currencyCryptsy, new ExchangeRate(currencyCryptsy, Utils.toNanoCoins(euros), URLCryptsy.getHost()));
                if(currencyCryptsy.equalsIgnoreCase("BTC")) btcRate = averageTrade;

                lastBitcoinValue = btcRate;
                lastBitcoinValueCheck = now;
            }
            finally
            {
                if (reader != null)
                    reader.close();
            }
            return btcRate;
        }
        catch (final IOException x)
        {
            x.printStackTrace();
        }
        catch (final JSONException x)
        {
            x.printStackTrace();
        }

        return null;
    }
    private static Object getCoinValueBTC_cryptopia()
    {
        //final Map<String, ExchangeRate> rates = new TreeMap<String, ExchangeRate>();
        // Keep the LTC rate around for a bit
        Double btcRate = 0.0;
        String currency = "BTC";
        String url = "https://www.cryptopia.co.nz/api/GetMarket/2623";





        try {
            // final String currencyCode = currencies[i];
            final URL URL_bter = new URL(url);
            final HttpURLConnection connection = (HttpURLConnection)URL_bter.openConnection();
            connection.setConnectTimeout(Constants.HTTP_TIMEOUT_MS * 2);
            connection.setReadTimeout(Constants.HTTP_TIMEOUT_MS * 2);
            connection.connect();

            final StringBuilder content = new StringBuilder();

            Reader reader = null;
            try
            {
                reader = new InputStreamReader(new BufferedInputStream(connection.getInputStream(), 1024));
                IOUtils.copy(reader, content);
                final JSONObject head = new JSONObject(content.toString());

				/*{
					"Success":true,
						"Message":null,
						"Data":{
							"TradePairId":100,
							"Label":"LTC/BTC",
							"AskPrice":0.00006000,
							"BidPrice":0.02000000,
							"Low":0.00006000,
							"High":0.00006000,
							"Volume":1000.05639978,
							"LastPrice":0.00006000,
							"LastVolume":499.99640000,
							"BuyVolume":67003436.37658233,
							"SellVolume":67003436.37658233,
							"Change":-400.00000000
						}
				}*/
                String result = head.getString("Success");
                if(result.equals("true"))
                {
                    JSONObject dataObject = head.getJSONObject("Data");

                    Double averageTrade = Double.valueOf(0.0);
                    if(dataObject.get("Label").equals("GLD/BTC"))
                        averageTrade = dataObject.getDouble("LastPrice");


                    if(currency.equalsIgnoreCase("BTC"))
                        btcRate = averageTrade;
                }
                return btcRate;
            }
            finally
            {
                if (reader != null)
                    reader.close();
            }

        }
        catch (final IOException x)
        {
            x.printStackTrace();
        }
        catch (final JSONException x)
        {
            x.printStackTrace();
        }

        return null;
    }


	private static Map<String, ExchangeRate> getLitecoinChartsOld()
	{
        final Map<String, ExchangeRate> rates = new TreeMap<String, ExchangeRate>();
        // Keep the BTC rate around for a bit
        Double ltcRate = getGoldCoinValueLTC();



        Double btcRate = 0.0;
        try {
            String currencies[] = {"USD", "BTC"};//, "RUR"};
            String urls[] = {"https://btc-e.com/api/2/14/ticker", "https://btc-e.com/api/2/10/ticker", "https://btc-e.com/api/2/ltc_rur/ticker"};
            for(int i = 0; i < currencies.length; ++i) {
                final String currencyCode = currencies[i];
                final URL URL = new URL(urls[i]);
                final URLConnection connection = URL.openConnection();
                connection.setConnectTimeout(TIMEOUT_MS);
                connection.setReadTimeout(TIMEOUT_MS);
                connection.connect();
                final StringBuilder content = new StringBuilder();

                Reader reader = null;
                try
                {
                    reader = new InputStreamReader(new BufferedInputStream(connection.getInputStream(), 1024));
                    IOUtils.copy(reader, content);
                    final JSONObject head = new JSONObject(content.toString());
                    JSONObject ticker = head.getJSONObject("ticker");
                    Double avg = ticker.getDouble("avg")*ltcRate;
                    String euros = String.format("%.7f", avg);
                    // Fix things like 3,1250
                    euros = euros.replace(",", ".");
                    rates.put(currencyCode, new ExchangeRate(currencyCode, Utils.toNanoCoins(euros), URL.getHost()));
                    if(currencyCode.equalsIgnoreCase("BTC")) btcRate = avg;
                }
                finally
                {
                    if (reader != null)
                        reader.close();
                }
            }
            // Handle LTC/EUR special since we have to do maths
            final URL URL = new URL("https://btc-e.com/api/2/btc_eur/ticker");
            final URLConnection connection = URL.openConnection();
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.connect();
            final StringBuilder content = new StringBuilder();

            Reader reader = null;
            try
            {
                reader = new InputStreamReader(new BufferedInputStream(connection.getInputStream(), 1024));
                IOUtils.copy(reader, content);
                final JSONObject head = new JSONObject(content.toString());
                JSONObject ticker = head.getJSONObject("ticker");
                Double avg = ticker.getDouble("avg");
                // This is bitcoins priced in euros.  We want GLD!
                avg *= btcRate;
                String s_avg = String.format("%.8f", avg).replace(',', '.');
                rates.put("EUR", new ExchangeRate("EUR", Utils.toNanoCoins(s_avg), URL.getHost()));

                //Add LTC information
                s_avg = String.format("%.8f", ltcRate);
                rates.put("LTC", new ExchangeRate("LTC", Utils.toNanoCoins(s_avg.replace(",", ".")), URL.getHost()));
            } finally
            {
                if (reader != null)
                    reader.close();
            }
            return rates;
        }
		catch (final IOException x)
		{
			x.printStackTrace();
		}
		catch (final JSONException x)
		{
			x.printStackTrace();
		}

		return null;
	}

    private static Map<String, ExchangeRate> getLitecoinCharts()
    {
        final Map<String, ExchangeRate> rates = new TreeMap<String, ExchangeRate>();
        // Keep the BTC rate around for a bit
        Double btcRate = 0.0;

        Object result = getGoldCoinValueBTC();


        if(result == null) {
                return null;
        }

        else btcRate = (Double)result;

        //Double btcRate = 0.0;
        try {
            rates.put("BTC", new ExchangeRate("BTC", Utils.toNanoCoins(String.format("%.8f", btcRate).replace(",", ".")), "pubapi.cryptsy.com"));
            String currencies[] = {"USD"};//, "BTC"};//, "RUR"};
            String urls[] = {"https://btc-e.com/api/2/1/ticker"};//, "https://btc-e.com/api/2/14/ticker", "https://btc-e.com/api/2/ltc_rur/ticker"};
            for(int i = 0; i < currencies.length; ++i) {
                final String currencyCode = currencies[i];
                final URL URL = new URL(urls[i]);
                final URLConnection connection = URL.openConnection();
                connection.setConnectTimeout(TIMEOUT_MS);
                connection.setReadTimeout(TIMEOUT_MS);
                connection.connect();
                final StringBuilder content = new StringBuilder();

                Reader reader = null;
                try
                {
                    reader = new InputStreamReader(new BufferedInputStream(connection.getInputStream(), 1024));
                    IOUtils.copy(reader, content);
                    final JSONObject head = new JSONObject(content.toString());
                    JSONObject ticker = head.getJSONObject("ticker");
                    Double avg = ticker.getDouble("avg")*btcRate;
                    String euros = String.format("%.8f", avg);
                    // Fix things like 3,1250
                    euros = euros.replace(",", ".");
                    rates.put(currencyCode, new ExchangeRate(currencyCode, Utils.toNanoCoins(euros), URL.getHost()));
                    if(currencyCode.equalsIgnoreCase("BTC")) btcRate = avg;
                }
                finally
                {
                    if (reader != null)
                        reader.close();
                }
            }
            // Handle LTC/EUR special since we have to do maths
            final URL URL = new URL("https://btc-e.com/api/2/btc_eur/ticker");
            final URLConnection connection = URL.openConnection();
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.connect();
            final StringBuilder content = new StringBuilder();

            Reader reader = null;
            try
            {
                reader = new InputStreamReader(new BufferedInputStream(connection.getInputStream(), 1024));
                IOUtils.copy(reader, content);
                final JSONObject head = new JSONObject(content.toString());
                JSONObject ticker = head.getJSONObject("ticker");
                Double avg = ticker.getDouble("avg");
                // This is bitcoins priced in euros.  We want GLD!
                avg *= btcRate;
                String s_avg = String.format("%.8f", avg).replace(',', '.');
                rates.put("EUR", new ExchangeRate("EUR", Utils.toNanoCoins(s_avg), URL.getHost()));

                //Add LTC information
                //s_avg = String.format("%.8f", ltcRate);
                //rates.put("LTC", new ExchangeRate("LTC", Utils.toNanoCoins(s_avg), URL.getHost()));
            } finally
            {
                if (reader != null)
                    reader.close();
            }
            return rates;
        }
        catch (final IOException x)
        {
            x.printStackTrace();
        }
        catch (final JSONException x)
        {
            x.printStackTrace();
        }

        return null;
    }

    private static Map<String, ExchangeRate> getBitcoinCharts()
    {
        try
        {
           // double btcRate = getGoldCoinValueBTC();     //NullPointerException???
            Double btcRate = 0.0;

            Object result = getGoldCoinValueBTC_ccex();


            if(result == null) {
                result = getCoinValueBTC_cryptopia();
                if(result == null)
                    return null;
                else btcRate = (Double)result;
            }

            else btcRate = (Double)result;

            final URL URL = new URL("http://api.bitcoincharts.com/v1/weighted_prices.json");
            final HttpURLConnection connection = (HttpURLConnection) URL.openConnection();
            connection.setConnectTimeout(Constants.HTTP_TIMEOUT_MS);
            connection.setReadTimeout(Constants.HTTP_TIMEOUT_MS);
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                return null;

            Reader reader = null;
            try
            {
                reader = new InputStreamReader(new BufferedInputStream(connection.getInputStream(), 1024));
                final StringBuilder content = new StringBuilder();
                IOUtils.copy(reader, content);

                final Map<String, ExchangeRate> rates = new TreeMap<String, ExchangeRate>();

                //Add Bitcoin information
                rates.put("BTC", new ExchangeRate("BTC", Utils.toNanoCoins(String.format("%.8f", btcRate).replace(",", ".")), "pubapi.cryptsy.com"));

                final JSONObject head = new JSONObject(content.toString());
                for (final Iterator<String> i = head.keys(); i.hasNext();)
                {
                    final String currencyCode = i.next();
                    if (!"timestamp".equals(currencyCode))
                    {
                        final JSONObject o = head.getJSONObject(currencyCode);

                        String rate = o.optString("24h", null);
                        if (rate == null)
                            rate = o.optString("7d", null);
                        if (rate == null)
                            rate = o.optString("30d", null);

                        double rateForBTC = Double.parseDouble(rate);

                        rate = String.format("%.8f", rateForBTC * btcRate);

                        if (rate != null)
                            rates.put(currencyCode, new ExchangeRate(currencyCode, Utils.toNanoCoins(rate.replace(",", ".")), URL.getHost()));
                    }
                }

                return rates;
            }
            finally
            {
                if (reader != null)
                    reader.close();
            }
        }
        catch (final IOException x)
        {
           // log.debug("problem reading exchange rates", x);
            x.printStackTrace();
        }
        catch (final JSONException x)
        {
            //log.debug("problem reading exchange rates", x);
            x.printStackTrace();
        }

        return null;
    }


    private static Map<String, ExchangeRate> getBlockchainInfo()
	{
		try
		{
            //double btcRate = getGoldCoinValueBTC();

            Double btcRate = 0.0;

            Object result = getGoldCoinValueBTC();

            if(result == null)
                return null;

            else btcRate = (Double)result;

			final URL URL = new URL("https://blockchain.info/ticker");
			final URLConnection connection = URL.openConnection();
			connection.setConnectTimeout(TIMEOUT_MS);
			connection.setReadTimeout(TIMEOUT_MS);
			connection.connect();
			final StringBuilder content = new StringBuilder();

			Reader reader = null;
			try
			{
				reader = new InputStreamReader(new BufferedInputStream(connection.getInputStream(), 1024));
				IOUtils.copy(reader, content);

				final Map<String, ExchangeRate> rates = new TreeMap<String, ExchangeRate>();

                //Add Bitcoin information
                rates.put("BTC", new ExchangeRate("BTC", Utils.toNanoCoins(String.format("%.8f", btcRate).replace(",", ".")), "pubapi.cryptsy.com"));

				final JSONObject head = new JSONObject(content.toString());
				for (final Iterator<String> i = head.keys(); i.hasNext();)
				{
					final String currencyCode = i.next();
					final JSONObject o = head.getJSONObject(currencyCode);
                    double gldInCurrency = o.getDouble("15m") * btcRate;
					final String rate = String.format("%.8f",gldInCurrency); //o.optString("15m", null);

					if (rate != null)
						rates.put(currencyCode, new ExchangeRate(currencyCode, Utils.toNanoCoins(rate.replace(",", ".")), URL.getHost()));
				}

				return rates;
			}
			finally
			{
				if (reader != null)
					reader.close();
			}
		}
		catch (final IOException x)
		{
			x.printStackTrace();
		}
		catch (final JSONException x)
		{
			x.printStackTrace();
		}

		return null;
	}

	// https://bitmarket.eu/api/ticker
}
