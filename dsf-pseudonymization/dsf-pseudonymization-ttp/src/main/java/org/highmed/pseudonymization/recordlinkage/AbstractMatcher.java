package org.highmed.pseudonymization.recordlinkage;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.DoubleStream;

public abstract class AbstractMatcher<P extends Person>
{
	private static class MatchResult<P extends Person> implements Comparable<MatchResult<P>>
	{
		final double result;
		final MatchedPerson<P> matchedPerson;

		MatchResult(double result, MatchedPerson<P> matchedPerson)
		{
			this.result = result;
			this.matchedPerson = matchedPerson;
		}

		MatchedPerson<P> getMatchedPerson()
		{
			return matchedPerson;
		}

		@Override
		public int compareTo(MatchResult<P> o)
		{
			return Double.compare(result, o.result);
		}

		boolean isResultAboveThreshold(double threshold)
		{
			return result >= threshold;
		}
	}

	public static final double DEFAULT_POSITIVE_MATCH_THRESHOLD = 0.8;

	private final MatchedPersonFactory<P> matchedPersonFactory;

	private final MatchCalculator matchCalculator;
	private final double positiveMatchThreshold;

	/**
	 * Uses {@value #DEFAULT_POSITIVE_MATCH_THRESHOLD} as the {@link #positiveMatchThreshold}.<br>
	 * Uses {@link MatchCalculatorStrategy#MIN} as the {@link #matchCalculator}
	 *
	 * @param matchedPersonFactory
	 *            not <code>null</code>
	 */
	protected AbstractMatcher(MatchedPersonFactory<P> matchedPersonFactory)
	{
		this(matchedPersonFactory, MatchCalculatorStrategy.MIN, DEFAULT_POSITIVE_MATCH_THRESHOLD);
	}

	/**
	 * Uses {@value #DEFAULT_POSITIVE_MATCH_THRESHOLD} as the {@link #positiveMatchThreshold}
	 *
	 * @param matchedPersonFactory
	 *            not <code>null</code>
	 * @param matchCalculator
	 *            not <code>null</code>
	 */
	protected AbstractMatcher(MatchedPersonFactory<P> matchedPersonFactory, MatchCalculator matchCalculator)
	{
		this(matchedPersonFactory, matchCalculator, DEFAULT_POSITIVE_MATCH_THRESHOLD);
	}

	/**
	 * @param matchedPersonFactory
	 *            not <code>null</code>
	 * @param matchCalculator
	 *            not <code>null</code>
	 * @param positiveMatchThreshold
	 *            {@code >=0}
	 */
	protected AbstractMatcher(MatchedPersonFactory<P> matchedPersonFactory, MatchCalculator matchCalculator,
			double positiveMatchThreshold)
	{
		this.matchedPersonFactory = Objects.requireNonNull(matchedPersonFactory, "matchedPersonFactory");
		this.matchCalculator = Objects.requireNonNull(matchCalculator, "matchCalculator");
		this.positiveMatchThreshold = positiveMatchThreshold;
	}

	protected final Function<? super P, MatchedPerson<P>> matchPerson(
			Collection<? extends MatchedPerson<P>> matchedPersons)
	{
		return person -> matchPerson(person, matchedPersons);
	}

	protected final MatchedPerson<P> matchPerson(P person, Collection<? extends MatchedPerson<P>> matchedPersons)
	{
		Optional<MatchResult<P>> bestMatch = matchedPersons.parallelStream().map(matchCalculator.calculateMatch(person))
				.filter(m -> m.isResultAboveThreshold(positiveMatchThreshold)).max(MatchResult::compareTo);

		Optional<MatchedPerson<P>> matchedPerson = bestMatch.map(MatchResult::getMatchedPerson);

		return matchedPerson.map(add(person)).orElseGet(() -> matchedPersonFactory.create(person));
	}

	private Function<MatchedPerson<P>, MatchedPerson<P>> add(P person)
	{
		return matchedPerson ->
		{
			matchedPerson.addMatch(person);
			return matchedPerson;
		};
	}

	protected final Function<P, MatchedPerson<P>> toMatchedPerson()
	{
		return person -> matchedPersonFactory.create(person);
	}

	protected final MatchedPerson<P> create(P person)
	{
		return matchedPersonFactory.create(person);
	}

	public static interface MatchCalculator
	{
		/**
		 * @param matchedPerson
		 *            not <code>null</code>
		 * @param <P>
		 *            the person type
		 * @param personToMatch
		 *            not <code>null</code>
		 * @return {@link MatchResult} containing the {@link MatchedPerson} and a match classification score
		 * @throws NoSuchElementException
		 *             if the list of {@link Person}s inside {@link MatchedPerson} is empty
		 */
		<P extends Person> MatchResult<P> calculateMatch(MatchedPerson<P> matchedPerson, P personToMatch)
				throws NoSuchElementException;

		/**
		 * @param <P>
		 *            the person type
		 * @param personToMatch
		 *            not <code>null</code>
		 * @return Function for {@link #calculateMatch(MatchedPerson, Person)}
		 */
		default <P extends Person> Function<MatchedPerson<P>, MatchResult<P>> calculateMatch(P personToMatch)
		{
			return matchedPerson -> calculateMatch(matchedPerson, personToMatch);
		}
	}

	public static enum MatchCalculatorStrategy implements MatchCalculator
	{
		/**
		 * Matches against the first person already matched
		 *
		 * @see MatchedPerson#getMatches()
		 */
		FIRST
		{
			@Override
			public <P extends Person> MatchResult<P> calculateMatch(MatchedPerson<P> matchedPerson, P personToMatch)
			{
				double firstMatch = matchedPerson.getFirstMatch().compareTo(personToMatch);
				return new MatchResult<P>(firstMatch, matchedPerson);
			}
		},
		/**
		 * Matches against the last person already matched
		 *
		 * @see MatchedPerson#getMatches()
		 */
		LAST
		{
			@Override
			public <P extends Person> MatchResult<P> calculateMatch(MatchedPerson<P> matchedPerson, P personToMatch)
			{
				double lastMatch = matchedPerson.getLastMatch().compareTo(personToMatch);
				return new MatchResult<P>(lastMatch, matchedPerson);
			}
		},
		/**
		 * Matches against all persons already matched and selects the maximum (best) match
		 *
		 * @see MatchedPerson#getMatches()
		 */
		MAX
		{
			@Override
			public <P extends Person> MatchResult<P> calculateMatch(MatchedPerson<P> matchedPerson, P personToMatch)
			{
				double maxMatch = matchedPerson.getMatches().stream().mapToDouble(p -> p.compareTo(personToMatch)).max()
						.orElseThrow();
				return new MatchResult<P>(maxMatch, matchedPerson);
			}
		},
		/**
		 * Matches against all persons already matched and selects the minimum (worst) match
		 *
		 * @see MatchedPerson#getMatches()
		 */
		MIN
		{
			@Override
			public <P extends Person> MatchResult<P> calculateMatch(MatchedPerson<P> matchedPerson, P personToMatch)
			{
				double minMatch = matchedPerson.getMatches().stream().mapToDouble(p -> p.compareTo(personToMatch)).min()
						.orElseThrow();
				return new MatchResult<P>(minMatch, matchedPerson);
			}
		},
		/**
		 * Matches against all persons already matched and calculates the average match
		 *
		 * @see MatchedPerson#getMatches()
		 */
		AVG
		{
			@Override
			public <P extends Person> MatchResult<P> calculateMatch(MatchedPerson<P> matchedPerson, P personToMatch)
			{
				double avgMatch = matchedPerson.getMatches().stream().mapToDouble(p -> p.compareTo(personToMatch))
						.average().orElseThrow();
				return new MatchResult<P>(avgMatch, matchedPerson);
			}
		},
		/**
		 * Matches against all persons already matched and calculates the median match
		 *
		 * @see MatchedPerson#getMatches()
		 */
		MEDIAN
		{
			@Override
			public <P extends Person> MatchResult<P> calculateMatch(MatchedPerson<P> matchedPerson, P personToMatch)
			{
				if (matchedPerson.getMatches().isEmpty())
					throw new NoSuchElementException();

				DoubleStream sorted = matchedPerson.getMatches().stream().mapToDouble(p -> p.compareTo(personToMatch))
						.sorted();

				double median = matchedPerson.getMatches().size() % 2 == 0
						? sorted.skip(matchedPerson.getMatches().size() / 2 - 1).limit(2).average().getAsDouble()
						: sorted.skip(matchedPerson.getMatches().size() / 2).findFirst().getAsDouble();

				return new MatchResult<P>(median, matchedPerson);
			}
		},
		/**
		 * Execute no matching
		 *
		 * @see MatchedPerson#getMatches()
		 */
		NONE
		{
			@Override
			public <P extends Person> MatchResult<P> calculateMatch(MatchedPerson<P> matchedPerson, P personToMatch)
			{
				return new MatchResult<P>(0.0, matchedPerson);
			}
		}
	}
}