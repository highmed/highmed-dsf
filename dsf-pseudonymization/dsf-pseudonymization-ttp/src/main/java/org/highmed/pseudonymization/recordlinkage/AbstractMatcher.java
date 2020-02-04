package org.highmed.pseudonymization.recordlinkage;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.DoubleStream;

public abstract class AbstractMatcher
{
	private static class MatchResult implements Comparable<MatchResult>
	{
		final double result;
		final MatchedPerson matchedPerson;

		MatchResult(double result, MatchedPerson matchedPerson)
		{
			this.result = result;
			this.matchedPerson = matchedPerson;
		}

		MatchedPerson getMatchedPerson()
		{
			return matchedPerson;
		}

		@Override
		public int compareTo(MatchResult o)
		{
			return Double.compare(result, o.result);
		}

		boolean isResultAboveThreshold(double threshold)
		{
			return result >= threshold;
		}
	}

	public static final double DEFAULT_POSITIVE_MATCH_THRESHOLD = 0.95;

	private final MatchCalculator matchCalculator;
	private final double positiveMatchThreshold;

	/**
	 * Uses {@value #DEFAULT_POSITIVE_MATCH_THRESHOLD} as the {@link #positiveMatchThreshold}.<br>
	 * Uses {@link MatchCalculatorStrategy#MIN} as the {@link #matchCalculator}
	 * 
	 */
	protected AbstractMatcher()
	{
		this(MatchCalculatorStrategy.MIN, DEFAULT_POSITIVE_MATCH_THRESHOLD);
	}

	/**
	 * Uses {@value #DEFAULT_POSITIVE_MATCH_THRESHOLD} as the {@link #positiveMatchThreshold}
	 * 
	 * @param matchCalculator
	 *            not <code>null</code>
	 */
	protected AbstractMatcher(MatchCalculator matchCalculator)
	{
		this(matchCalculator, DEFAULT_POSITIVE_MATCH_THRESHOLD);
	}

	/**
	 * @param matchCalculator
	 *            not <code>null</code>
	 * @param positiveMatchThreshold
	 */
	protected AbstractMatcher(MatchCalculator matchCalculator, double positiveMatchThreshold)
	{
		this.matchCalculator = matchCalculator;
		this.positiveMatchThreshold = positiveMatchThreshold;
	}

	protected final Function<? super Person, MatchedPerson> matchPerson(
			Collection<? extends MatchedPerson> matchedPersons)
	{
		return person -> matchPerson(person, matchedPersons);
	}

	protected final MatchedPerson matchPerson(Person person, Collection<? extends MatchedPerson> matchedPersons)
	{
		Optional<MatchResult> bestMatch = matchedPersons.parallelStream().map(matchCalculator.calculateMatch(person))
				.filter(m -> m.isResultAboveThreshold(positiveMatchThreshold)).max(MatchResult::compareTo);

		Optional<MatchedPerson> matchedPerson = bestMatch.map(MatchResult::getMatchedPerson);

		return matchedPerson.map(MatchedPerson.add(person)).orElseGet(MatchedPerson.from(person));
	}

	public static interface MatchCalculator
	{
		/**
		 * @param matchedPerson
		 *            not <code>null</code>
		 * @param personToMatch
		 *            not <code>null</code>
		 * @return
		 * @throws NoSuchElementException
		 *             if the list of {@link Person}s inside {@link MatchedPerson} is empty
		 */
		MatchResult calculateMatch(MatchedPerson matchedPerson, Person personToMatch) throws NoSuchElementException;

		/**
		 * @param personToMatch
		 *            not <code>null</code>
		 * @return Function for {@link #calculateMatch(MatchedPerson, Person)}
		 */
		default Function<MatchedPerson, MatchResult> calculateMatch(Person personToMatch)
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
			public MatchResult calculateMatch(MatchedPerson matchedPerson, Person personToMatch)
			{
				double firstMatch = matchedPerson.getFirstMatch().compareTo(personToMatch);
				return new MatchResult(firstMatch, matchedPerson);
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
			public MatchResult calculateMatch(MatchedPerson matchedPerson, Person personToMatch)
			{
				double lastMatch = matchedPerson.getLastMatch().compareTo(personToMatch);
				return new MatchResult(lastMatch, matchedPerson);
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
			public MatchResult calculateMatch(MatchedPerson matchedPerson, Person personToMatch)
			{
				double maxMatch = matchedPerson.getMatches().stream().mapToDouble(p -> p.compareTo(personToMatch)).max()
						.orElseThrow();
				return new MatchResult(maxMatch, matchedPerson);
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
			public MatchResult calculateMatch(MatchedPerson matchedPerson, Person personToMatch)
			{
				double minMatch = matchedPerson.getMatches().stream().mapToDouble(p -> p.compareTo(personToMatch)).min()
						.orElseThrow();
				return new MatchResult(minMatch, matchedPerson);
			}
		},
		/**
		 * Matches against all personss already matched and calculates the average match
		 * 
		 * @see MatchedPerson#getMatches()
		 */
		AVG
		{
			@Override
			public MatchResult calculateMatch(MatchedPerson matchedPerson, Person personToMatch)
			{
				double avgMatch = matchedPerson.getMatches().stream().mapToDouble(p -> p.compareTo(personToMatch))
						.average().orElseThrow();
				return new MatchResult(avgMatch, matchedPerson);
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
			public MatchResult calculateMatch(MatchedPerson matchedPerson, Person personToMatch)
			{
				if (matchedPerson.getMatches().isEmpty())
					throw new NoSuchElementException();

				DoubleStream sorted = matchedPerson.getMatches().stream().mapToDouble(p -> p.compareTo(personToMatch))
						.sorted();

				double median = matchedPerson.getMatches().size() % 2 == 0
						? sorted.skip(matchedPerson.getMatches().size() / 2 - 1).limit(2).average().getAsDouble()
						: sorted.skip(matchedPerson.getMatches().size() / 2).findFirst().getAsDouble();

				return new MatchResult(median, matchedPerson);
			}
		}
	}
}
