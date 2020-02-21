package org.highmed.pseudonymization.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Pseudonym
{
	private final List<TtpId> ids = new ArrayList<>();
	private final String padding;

	public Pseudonym(Collection<? extends TtpId> ids, int paddingLength)
	{
		if (ids != null)
		{
			this.ids.addAll(ids);
		}
		else
		{
			throw new IllegalArgumentException("Collection of TTP-IDs must not be null.");
		}
		if (paddingLength > 0)
		{
			this.padding = StringUtils.repeat(" ", paddingLength);
		}
		else
		{
			this.padding = "";
		}
	}

	@JsonCreator
	private Pseudonym(
			@JsonProperty("ids")
					Collection<? extends TtpId> ids,
			@JsonProperty("padding")
					String padding)
	{
		if (ids != null)
		{
			this.ids.addAll(ids);
		}
		else
		{
			throw new IllegalArgumentException("Collection of TTP-IDs must not be null.");
		}
		if (padding != null)
		{
			this.padding = padding;
		}
		else
		{
			throw new IllegalArgumentException("JSON-serialized String contained no padding field.");
		}
	}

	public Pseudonym(Collection<? extends TtpId> ids)
	{
		this(ids, 0);
	}

	public Pseudonym withPadding(int length)
	{
		return new Pseudonym(this.ids, length);
	}

	public List<TtpId> getIds()
	{
		return Collections.unmodifiableList(ids);
	}

	public String getPadding()
	{
		return padding;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (!(o instanceof Pseudonym))
			return false;
		Pseudonym pseudonym = (Pseudonym) o;
		return ids.equals(pseudonym.ids) && padding.equals(pseudonym.padding);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(ids, padding);
	}
}
